package com.github.spriet2000.vertx.handlers.http.server.ext.impl;

import io.vertx.core.http.HttpServerRequest;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ResponseTimeHandler<T> implements BiFunction<Consumer<Throwable>, Consumer<Object>, BiConsumer<HttpServerRequest, T>> {

    @Override
    public BiConsumer<HttpServerRequest, T> apply(Consumer<Throwable> fail, Consumer<Object> next) {
        return (req, arg) -> {
            long start = System.nanoTime();
            req.response().headersEndHandler(e -> {
                if (!req.response().headWritten()) {
                    req.response().headers().add("X-Response-Time",
                            String.format("%sms", (System.nanoTime() - start) / (double) 1000000));
                    e.complete();
                }
            });
            next.accept(arg);
        };
    }
}
