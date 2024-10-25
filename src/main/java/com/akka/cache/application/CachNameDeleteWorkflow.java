package com.akka.cache.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.workflow.Workflow;
import com.akka.cache.domain.DeleteCacheNameState;
import com.typesafe.config.Config;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ComponentId("cach_name_delete_workflow")
public class CachNameDeleteWorkflow extends Workflow<DeleteCacheNameState> {

    private final Config config;

    public CachNameDeleteWorkflow(Config config) {
        this.config = config;
    }

    /* use this method to process a configurable chunk of futures, and repeat until chunking is done */
    /* We should probably move this into a static class lib, so it can be easily re-used */
    public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture<Void> allFuturesResult =
                CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0]));
        return allFuturesResult.thenApply(v ->
                futuresList.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.<T>toList())
        );
    }

    @Override
    public WorkflowDef<DeleteCacheNameState> definition() {
        // TODO: collect the batch of cacheIDs from the CacheView
        // TODO: then chunk (configurable) through the list deleting each CacheEnttity repeating until done
        return null;
    }
}
