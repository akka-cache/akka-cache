package io.akka.cache.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

public class FutureHelper {

    /*
    If any of the given CompletableFutures complete exceptionally,
    then the returned CompletableFuture also does so,
    with a CompletionException holding this exception as its cause.
     */
    public static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture<Void> allFuturesResult =
                CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[0]));
        return allFuturesResult.thenApply(v ->
                futuresList.stream().
                        map(CompletableFuture::join).
                            collect(Collectors.<T>toList())
        );
    }
}
