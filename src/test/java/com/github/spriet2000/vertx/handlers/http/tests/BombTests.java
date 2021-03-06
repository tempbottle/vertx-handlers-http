package com.github.spriet2000.vertx.handlers.http.tests;


import com.github.spriet2000.handlers.BiHandlers;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.HttpTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import static com.github.spriet2000.handlers.BiHandlers.compose;

public class BombTests extends HttpTestBase {

    Logger logger = LoggerFactory.getLogger(BombTests.class);

    @Before
    public void setup() {
        server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
        client = vertx.createHttpClient(new HttpClientOptions());
    }

    @Test
    public void bomb() {

        BiConsumer<HttpServerRequest, Throwable> exception = (e, a) -> logger.error(a);
        BiConsumer<HttpServerRequest, Object> success = (e, a) -> logger.info(a);

        BiHandlers<HttpServerRequest, Object> handlers = compose(
                (f, n) -> (e, a) -> e.response().end());


        int bombs = 2000;
        CountDownLatch startSignal = new CountDownLatch(bombs);
        server.requestHandler(req -> handlers
                .apply(exception, success)
                .accept(req, null)).listen(onSuccess(s -> {
            client = vertx.createHttpClient(new HttpClientOptions());
            for (int i = 0; i < bombs; i++) {
                client.getNow(8080, "localhost", "/test",
                        res -> {
                            logger.info(startSignal.getCount());
                            assertEquals(200, res.statusCode());
                            startSignal.countDown();
                        });
            }
        }));

        try {
            startSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
