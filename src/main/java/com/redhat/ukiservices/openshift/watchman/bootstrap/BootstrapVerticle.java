package com.redhat.ukiservices.openshift.watchman.bootstrap;

import com.redhat.ukiservices.openshift.watchman.persistence.InfluxDBVerticle;
import com.redhat.ukiservices.openshift.watchman.watch.WatchmanVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();

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


    }

}
