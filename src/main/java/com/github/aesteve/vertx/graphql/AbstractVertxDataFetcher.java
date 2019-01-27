package com.github.aesteve.vertx.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

abstract public class AbstractVertxDataFetcher<T> implements DataFetcher<CompletableFuture<T>> {

    protected final Vertx vertx;

    protected AbstractVertxDataFetcher(Vertx vertx) {
        this.vertx = vertx;
    }

    public abstract Future<T> fetch(DataFetchingEnvironment environment);

    @Override
    public CompletableFuture<T> get(DataFetchingEnvironment environment) {
        CompletableFuture<T> fut = new CompletableFuture<>();
        vertx.runOnContext(_v ->
                fetch(environment).setHandler(ar -> {
                    if (ar.failed()) {
                        fut.completeExceptionally(ar.cause());
                    } else {
                        fut.complete(ar.result());
                    }
                })
        );
        return fut;
    }
}
