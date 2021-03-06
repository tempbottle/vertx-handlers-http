package com.github.spriet2000.vertx.handlers.http.server.ext.bodyParser.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.spriet2000.vertx.handlers.http.server.ext.bodyParser.Body;
import com.github.spriet2000.vertx.handlers.http.server.ext.bodyParser.BodyParseException;
import io.netty.handler.codec.http.HttpHeaders;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class JsonBodyParser<A extends Body> implements BiFunction<BiConsumer<HttpServerRequest, Throwable>, BiConsumer<HttpServerRequest, A>,
        BiConsumer<HttpServerRequest, A>> {

    private final Class clazz;

    public JsonBodyParser(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public BiConsumer<HttpServerRequest, A> apply(BiConsumer<HttpServerRequest, Throwable> fail,
                                                  BiConsumer<HttpServerRequest, A> next) {
        return (req, arg) -> {
            if (req.headers().contains(HttpHeaders.Names.CONTENT_TYPE)
                    && !req.headers().get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json")) {
                next.accept(req, arg);
                return;
            }
            if (req.method() == HttpMethod.GET
                    || req.method() == HttpMethod.HEAD) {
                next.accept(req, arg);
            } else {
                Buffer body = Buffer.buffer();
                req.handler(body::appendBuffer);
                req.endHandler(e -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        arg.body(mapper.readValue(body.toString(), clazz));
                        next.accept(req, arg);
                    } catch (Exception exception) {
                        fail.accept(req, new BodyParseException(exception));
                    }
                });
            }
        };
    }
}
