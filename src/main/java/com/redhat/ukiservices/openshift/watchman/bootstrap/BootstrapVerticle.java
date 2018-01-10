package com.redhat.ukiservices.openshift.watchman.bootstrap;

import com.redhat.ukiservices.openshift.watchman.persistence.InfluxDBVerticle;
import com.redhat.ukiservices.openshift.watchman.watch.WatchmanVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapVerticle.class);

    private boolean online;

    @Override
    public void start() throws Exception {
        super.start();

        Router router = Router.router(vertx);

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("server-online",
                fut -> fut.complete(online ? Status.OK() : Status.KO()));

        router.get("/health").handler(healthCheckHandler);

        vertx.deployVerticle(InfluxDBVerticle.class.getName(), res -> {
            if (res.failed()) {
                logger.error("Initialisation failed", res.cause());
            }

        });

        vertx.deployVerticle(WatchmanVerticle.class.getName(), res -> {
            if (res.failed()) {
                logger.error("Initialisation failed", res.cause());
            }

        });

        HttpServer server = vertx.createHttpServer().requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), ar -> {
                    online = ar.succeeded();
                });
    }

}
