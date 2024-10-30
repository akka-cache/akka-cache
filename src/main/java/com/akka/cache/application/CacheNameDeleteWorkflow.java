package com.akka.cache.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import akka.javasdk.workflow.Workflow;
import com.akka.cache.domain.DeleteCacheNameState;
import com.akka.cache.lib.FutureHelper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ComponentId("cach_name_delete_workflow")
public class CacheNameDeleteWorkflow extends Workflow<DeleteCacheNameState> {
    private static final Logger log = LoggerFactory.getLogger(CacheNameDeleteWorkflow.class);

    private final Config config;
    private final ComponentClient componentClient;
    private final TimerScheduler timerScheduler;
    private final int deleteBlockSize;

    public CacheNameDeleteWorkflow(Config config, ComponentClient componentClient, TimerScheduler timerScheduler) {
        this.config = config;
        this.componentClient = componentClient;
        this.timerScheduler = timerScheduler;
        deleteBlockSize = config.getInt("cache-name-delete-block-size");
    }

    public Effect<Done> startDeletions(String cacheName) {
        DeleteCacheNameState initialState = new DeleteCacheNameState(cacheName);
        return effects()
                .updateState(initialState)
                .transitionTo("collectCacheIds")
                .thenReply(Done.done());
    }

    private CompletableFuture<String> deleteCache(String key) {
        return componentClient.forKeyValueEntity(currentState().cacheName())
                .method(CacheNameEntity::delete)
                .invokeAsync()
                .thenApply(c -> key).toCompletableFuture();
    }

    // note: cacheName Param not used as it's already in current state if needed
    private CompletableFuture<CacheView.CachedKeys> deleteBatch(String cacheName) {
        int batchPosition = currentState().deleted().keys().size();
        int maxToDo = batchPosition + deleteBlockSize;
        int remaining = currentState().keys().keys().size() - batchPosition;
        if (remaining < maxToDo) {
            maxToDo = remaining;
        }
        List<CompletableFuture<String>> deleteKeysFutures = new ArrayList<>();
        for (int i = batchPosition; i < maxToDo; i++) {
            deleteKeysFutures.add(deleteCache(currentState().keys().keys().get(i)));
        }
        // wait for all the futures to complete
        return FutureHelper.allOf(deleteKeysFutures)
                .thenApply(keys -> {
                    return new CacheView.CachedKeys(keys);
                });
    }

    private CompletionStage<Done> killTTLTimer(String key) {
        String compoundKey = String.format("%s%s", currentState().cacheName(), key);
        return timerScheduler.cancel(compoundKey);
    }

    // note: cacheName Param not used as it's already in current state if needed
    private CompletionStage<Integer> killCachedTimers(String cacheName) {
        int maxToDo = currentState().killTTLPos() + deleteBlockSize;
        int remaining = currentState().keys().keys().size() - currentState().killTTLPos();
        if (remaining < maxToDo) {
            maxToDo = remaining;
        }
        List<CompletableFuture<Done>> killTTLFutures = new ArrayList<>();
        int iPos = currentState().killTTLPos();
        for (; iPos < maxToDo; iPos++) {
            killTTLFutures.add(killTTLTimer(currentState().deleted().keys().get(iPos)).toCompletableFuture());
        }
        final Integer finalPosition = Integer.valueOf(iPos);
        // wait for all the futures to complete
        return FutureHelper.allOf(killTTLFutures)
                .thenApply(keys -> {
                    return Integer.valueOf(finalPosition);
                });
    }

    @Override
    public WorkflowDef<DeleteCacheNameState> definition() {
        // collect the batch of cacheIDs from the CacheView
        Step collectCacheIds =
                step("collectCacheIds")
                        .asyncCall(() ->
                                componentClient.forView()
                                        .method(CacheView::getCacheKeys)
                                        .invokeAsync(currentState().cacheName()))
                                        .andThen(CacheView.CachedKeys.class, cachedKeys -> {
                                            return effects()
                                                    .updateState(currentState().withCached(cachedKeys))
                                                    .transitionTo("deleteCached");
                                        });

        Step deleteCached =
                step("deleteCached")
                        // just passing the cacheName
                        .asyncCall(() -> {
                            return deleteBatch(currentState().cacheName());
                        })
                        .andThen(CacheView.CachedKeys.class, keysDeleted -> {
                            if (keysDeleted.keys().size() == currentState().keys().keys().size()) {
                                // we're done move to next stage
                                return effects()
                                        .updateState(currentState().withDeleted(keysDeleted))
                                        .transitionTo("killTimers");
                            }
                            else {
                                // there's more to delete
                                return effects()
                                        .updateState(currentState().withDeleted(keysDeleted))
                                        .transitionTo("deleteCached");
                            }
                        });

        Step killTimers =
                step("killTimers")
                        .asyncCall(() -> {
                            return killCachedTimers(currentState().cacheName());
                        })
                        .andThen(Integer.class, killTTLPos -> {
                            if (currentState().deleted().keys().size() == killTTLPos) {
                                return effects()
                                        .updateState(currentState().withStatus(DeleteCacheNameState.DeleteStatus.COMPLETE))
                                        .transitionTo("deleteCacheName");
                            }
                            else {
                                return effects()
                                        .updateState(currentState().withKillTTLPos(killTTLPos))
                                        .transitionTo("killTimers");
                            }
                        }
                    );

        Step deleteCacheName =
                step("deleteCacheName")
                        .asyncCall(() -> {
                            return componentClient.forKeyValueEntity(currentState().cacheName())
                                    .method(CacheNameEntity::delete)
                                    .invokeAsync();
                        })
                        .andThen(Done.class, done -> effects().end());

        return workflow()
                .addStep(collectCacheIds)
                .addStep(deleteCached)
                .addStep(killTimers)
                .addStep(deleteCacheName);
    }
}
