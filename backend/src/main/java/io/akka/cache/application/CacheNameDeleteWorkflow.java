package io.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import akka.javasdk.workflow.Workflow;
import io.akka.cache.domain.DeleteCacheNameState;
import io.akka.cache.utils.FutureHelper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ComponentId("cache_name_delete_workflow")
public class CacheNameDeleteWorkflow extends Workflow<DeleteCacheNameState> {
    private static final Logger log = LoggerFactory.getLogger(CacheNameDeleteWorkflow.class);

    private static final String STEP1_COLLECT_CACHE_IDS = "s1";
    private static final String STEP2_DELETE_CACHE_IDS = "s2";
    private static final String STEP3_DELETE_CACHE_NAME = "s3";
    private static final String STEP4_END_WORKFLOW = "s4";

    private final Config config;
    private final ComponentClient componentClient;
    private final TimerScheduler timerScheduler;
    private final int deleteBlockSize;
    private final int maxIntRetries;

    public CacheNameDeleteWorkflow(Config config, ComponentClient componentClient, TimerScheduler timerScheduler) {
        this.config = config;
        this.componentClient = componentClient;
        this.timerScheduler = timerScheduler;
        this.deleteBlockSize = config.getInt("app.cache-name-delete-block-size");
        this.maxIntRetries = config.getInt("app.cache-name-delete-max-internal-retries");
        if (log.isDebugEnabled()) {
            log.debug("CacheNameDeleteWorkflow constructor deleteBlockSize is {}", deleteBlockSize);
        }
    }

    public record StartDeletionsSetup(String cacheName, Boolean flushOnly) {}

    public Effect<Done> startDeletions(StartDeletionsSetup startDeletionsSetup) {
        DeleteCacheNameState initialState = new DeleteCacheNameState(startDeletionsSetup.cacheName, startDeletionsSetup.flushOnly);
        return effects()
                .updateState(initialState)
                .transitionTo(STEP1_COLLECT_CACHE_IDS)
                .thenReply(Done.done());
    }

    private CompletableFuture<Done> deleteCache(String key) {
        String compoundKey = currentState().cacheName().concat(key);
        if (log.isDebugEnabled()) {
            log.debug("deleteCache requesting delete of cache {}", compoundKey);
        }
        return componentClient.forEventSourcedEntity(compoundKey)
                .method(CacheEntity::delete)
                .invokeAsync()
                .thenApply(c -> {
                    timerScheduler.cancel(compoundKey);
                    return Done.done();
                }).exceptionally(ex -> {
                    if (log.isDebugEnabled()) {
                       // if a cache doesn't actually exist don't stop the workflow
                    }
                    return Done.done();
                }).toCompletableFuture();
    }

    private record DeleteBatchResult(boolean success, int currOffset) {}

    // note: cacheName Param not used as it's already in current state if needed
    private CompletableFuture<DeleteBatchResult> deleteNextBatch() {
        int batchPosition = currentState().currOffset();
        int maxToDo = deleteBlockSize;
        int remaining = currentState().keys().keys().size() - batchPosition;
        if (remaining < maxToDo) {
            maxToDo = remaining;
        }
        if (log.isDebugEnabled()) {
            log.debug("deleteBatch deleteBlockSize {}", deleteBlockSize);
            log.debug("deleteBatch batchPosition {}", batchPosition);
            log.debug("deleteBatch maxToDo {}", maxToDo);
            log.debug("deleteBatch remaining {}", remaining);
        }
        List<CompletableFuture<Done>> deleteKeysFutures = new ArrayList<>();
        int offset = batchPosition;
        for (; offset < (batchPosition + maxToDo); offset++) {
            deleteKeysFutures.add(deleteCache(currentState().keys().keys().get(offset)));
        }
        final int newOffset = offset;
        // wait for all the futures to complete, if any futures fail with exception, then allOf returns with exception
        return FutureHelper.allOf(deleteKeysFutures)
                .thenApply(futures -> new DeleteBatchResult(true, newOffset))
                .exceptionally(ex -> {
                    log.error("an exception occurred while deleting batches: {}", ex.getMessage());
                    return new DeleteBatchResult(false, newOffset);
                });
    }

    protected Step getStepCollectCacheIds(String stepName, String transitionTo) {
        return step(stepName)
                .asyncCall(() ->
                        componentClient.forView()
                                .method(CacheView::getCacheKeys)
                                .invokeAsync(currentState().cacheName()))
                .andThen(CacheView.CachedKeys.class, cachedKeys -> effects()
                        .updateState(currentState().withCached(cachedKeys))
                        .transitionTo(transitionTo));
    }

    protected Step getStepDeleteCached(String stepName, String transitionTo, String transitionOnFlushOnly) {
        return step(stepName)
                .asyncCall(this::deleteNextBatch)
                .andThen(DeleteBatchResult.class, deleteBatchResult -> {
                    if (log.isDebugEnabled()) {
                        log.debug("{} deleteBatchResult success {}", stepName, deleteBatchResult.success);
                        log.debug("{} original keys size {}", stepName, currentState().keys().keys().size());
                        log.debug("{} result offset {}", stepName, deleteBatchResult.currOffset);
                    }
                    if (deleteBatchResult.success) {
                        if (deleteBatchResult.currOffset == currentState().keys().keys().size()) {
                            if (log.isDebugEnabled()) {
                                log.debug("{} were done. transitionTo {}", stepName, transitionTo);
                            }
                            // we're done move to next stage
                            if (currentState().flushOnly()) {
                                return effects()
                                        .updateState(currentState().withCurrOffset(deleteBatchResult.currOffset))
                                        .transitionTo(transitionOnFlushOnly);
                            }
                            return effects()
                                    .updateState(currentState().withCurrOffset(deleteBatchResult.currOffset))
                                    .transitionTo(transitionTo);
                        }
                        else {
                            if (log.isDebugEnabled()) {
                                log.debug("{} were not done, do next block", stepName);
                            }
                            // there's more to delete
                            return effects()
                                    .updateState(currentState().withCurrOffset(deleteBatchResult.currOffset))
                                    .transitionTo(stepName);
                        }
                    }
                    else {
                        // there was an exception, so we're going to start over
                        if (log.isDebugEnabled()) {
                            log.debug("{} experienced an error, restarting at the beginning of the flow.", stepName);
                        }
                        if ((currentState().intRetries()) == this.maxIntRetries) {
                            // we're out of here
                            // since flushOnly doesn't delete the cacheName, we'll transition there to stop
                            return effects()
                                    .transitionTo(transitionOnFlushOnly);
                        }
                        return effects()
                                .updateState(currentState().withIntRetries(currentState().intRetries()+1))
                                .transitionTo(STEP1_COLLECT_CACHE_IDS);
                    }
                });
    }

    protected Step getStepDeleteCacheName(String stepName) {
        return step(stepName)
                .asyncCall(() -> {
                    var result = componentClient.forEventSourcedEntity(currentState().cacheName())
                            .method(CacheNameEntity::delete)
                            .invokeAsync();
                    return result.thenApply(done -> {
                        if (log.isDebugEnabled()) {
                            log.debug("delete cacheName {}", currentState().cacheName());
                        }
                        return Done.done();
                    }).exceptionally(ex -> {
                        // if a cache doesn't actually exist don't stop the workflow
                        if (log.isDebugEnabled()) {
                            log.error("an exception occurred while deleting cacheName: {}. The cacheName may not have existed previously", ex.getMessage());
                        }
                        return Done.done();
                    });
                })
                .andThen(Done.class, done -> {
                    if (log.isDebugEnabled()) {
                        log.debug("getStepDeleteCacheName completed.");
                    }
                    return effects()
                            .updateState(currentState().withStatus(DeleteCacheNameState.DeleteStatus.COMPLETE))
                            .end();
                });
    }

    protected Step getStepEndDeleteWorkflow(String stepName) {
        return step(stepName)
                .asyncCall(() -> CompletableFuture.completedFuture(Done.done()))
                .andThen(Done.class, done -> {
                    if (log.isDebugEnabled()) {
                        log.debug("getStepEndWorkflow called.");
                    }
                    return effects()
                            .updateState(currentState().withStatus(DeleteCacheNameState.DeleteStatus.COMPLETE))
                            .end();
                });
    }

    @Override
    public WorkflowDef<DeleteCacheNameState> definition() {

        Step collectCacheIds = getStepCollectCacheIds(STEP1_COLLECT_CACHE_IDS, STEP2_DELETE_CACHE_IDS);
        Step deleteCached = getStepDeleteCached(STEP2_DELETE_CACHE_IDS, STEP3_DELETE_CACHE_NAME, STEP4_END_WORKFLOW);
        Step deleteCacheName = getStepDeleteCacheName(STEP3_DELETE_CACHE_NAME);
        Step end = getStepEndDeleteWorkflow(STEP4_END_WORKFLOW);
        return workflow()
                .addStep(collectCacheIds)
                .addStep(deleteCached)
                .addStep(deleteCacheName)
                .addStep(end);
    }
}
