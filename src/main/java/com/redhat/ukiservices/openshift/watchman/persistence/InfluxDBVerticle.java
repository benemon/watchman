package com.redhat.ukiservices.openshift.watchman.persistence;

import com.redhat.ukiservices.openshift.watchman.common.CommonConstants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InfluxDBVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBVerticle.class);

    private String influxDbHost;
    private String influxDbPort;
    private String influxDbDatabase;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        influxDbHost = System.getenv(CommonConstants.INFLUXDB_SERVICE_HOST_ENV) != null ? System.getenv(CommonConstants.INFLUXDB_SERVICE_HOST_ENV) : CommonConstants.INFLUXDB_SERVICE_HOST_DEFAULT;
        influxDbPort = System.getenv(CommonConstants.INFLUXDB_SERVICE_PORT_ENV) != null ? System.getenv(CommonConstants.INFLUXDB_SERVICE_PORT_ENV) : CommonConstants.INFLUXDB_SERVICE_PORT_DEFAULT;
        influxDbDatabase = System.getenv(CommonConstants.INFLUXDB_DATABASE_ENV) != null ? System.getenv(CommonConstants.INFLUXDB_DATABASE_ENV) : CommonConstants.INFLUXDB_DATABASE_DEFAULT;
    }

    @Override
    public void start() throws Exception {
        super.start();

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(CommonConstants.INFLUXDB_PERSISTENCE_ADDDRESS);

        consumer.handler(this::persistMessage);

    }

    private void persistMessage(Message<JsonObject> jsonObjectMessage) {

        JsonObject state = jsonObjectMessage.body();
        JsonObject replicas = state.getJsonObject("replicas");

        vertx.executeBlocking(future -> {
                    InfluxDB influxDB = InfluxDBFactory.connect(String.format("http://%s:%s", influxDbHost, influxDbPort));
                    influxDB.enableBatch(100, 100, TimeUnit.MILLISECONDS, Executors.defaultThreadFactory(), (failedPoints, throwable) -> {
                        future.fail(throwable);
                    });
                    influxDB.setDatabase(influxDbDatabase);

                    Point point = Point.measurement("replicas")
                            .time(state.getLong("timestamp"), TimeUnit.SECONDS)
                            .tag("project", state.getString("project"))
                            .tag("deployment", state.getString("name"))
                            .addField("available", replicas.getInteger("available"))
                            .addField("required", replicas.getInteger("required"))
                            .addField("unavailable", replicas.getInteger("unavailable"))
                            .build();

                    influxDB.write(point);
                    influxDB.close();

                    future.complete();

                }, res -> {
                    if (res.succeeded()) {
                        logger.info(String.format("Successfully wrote entry to InfluxDB: %n%s", state.encodePrettily()));
                    } else if (res.failed()) {
                        logger.error("Failed to write entry to InfluxDB", res.cause());
                    }
                }
        );
    }

}
