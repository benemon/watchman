package com.redhat.ukiservices.openshift.watchman.watch;

import com.redhat.ukiservices.openshift.watchman.common.CommonConstants;
import com.redhat.ukiservices.openshift.watchman.watch.handler.OpenShiftDeploymentConfigMessageParser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import static com.redhat.ukiservices.openshift.watchman.common.CommonConstants.DEFAULT_TOKEN_PATH;
import static com.redhat.ukiservices.openshift.watchman.common.CommonConstants.KUBERNETES_AUTH_TOKEN_ENV;

public class WatchmanVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(WatchmanVerticle.class);

    private String kubernetesHost;
    private Integer kubernetesPort;

    private Buffer current = Buffer.buffer();

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        kubernetesHost = System.getenv(CommonConstants.KUBERNETES_HOST_ENV) != null ? System.getenv(CommonConstants.KUBERNETES_HOST_ENV) : CommonConstants.KUBERNETES_HOST_DEFAULT;
        kubernetesPort = Integer.parseInt(CommonConstants.KUBERNETES_PORT_ENV != null ? System.getenv(CommonConstants.KUBERNETES_PORT_ENV) : CommonConstants.KUBERNETES_PORT_DEFAULT);
    }

    @Override
    public void start() throws Exception {
        super.start();

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultHost(kubernetesHost);
        httpClientOptions.setDefaultPort(kubernetesPort);
        httpClientOptions.setSsl(true);
        httpClientOptions.setTrustAll(true);

        logger.info(String.format("Connecting to %s:%d%s", kubernetesHost, kubernetesPort, CommonConstants.OPENSHIFT_API_ENDPOINT));

        HttpClient client = vertx.createHttpClient(httpClientOptions);
        client
                .get(CommonConstants.OPENSHIFT_API_ENDPOINT)
                .setChunked(true)
                .handler(resp -> resp.handler(this::handle))
                .exceptionHandler(Throwable::printStackTrace)
                .putHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", getAuthToken()))
                .putHeader(HttpHeaders.UPGRADE, "websocket")
                .end();
    }


    /**
     * Handle the message from the websocket by performing ETL and converting to a nicer format for the EB
     *
     * @param buffer
     */
    private void handle(Buffer buffer) {
        try {
            current = current.appendBuffer(buffer);
            JsonObject object = current.toJsonObject();
            Optional<JsonObject> stateChange = OpenShiftDeploymentConfigMessageParser.parseMessage(object);
            stateChange.ifPresent(this::publishStateChange);
            current = Buffer.buffer();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Publish JsonObject to the EB
     *
     * @param stateChange
     */
    private void publishStateChange(JsonObject stateChange) {
        vertx.eventBus().send(CommonConstants.INFLUXDB_PERSISTENCE_ADDDRESS, stateChange);
    }

    /**
     * Interrogate the runtime environment to find out if we're in OpenShift or not, and return the token as appropriate
     *
     * @return String containing a bearer token
     */
    private String getAuthToken() {
        // Find out if we're in OpenShift or not
        File tokenFile = new File(DEFAULT_TOKEN_PATH);

        String token = null;
        if (tokenFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(tokenFile));
                token = reader.readLine();

            } catch (IOException e) {
                logger.warn(String.format("Error reading service account token %s.", e.getMessage()));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        logger.warn(String.format("Error closing resources %s].", e.getMessage()));
                    }
                }

            }
        } else {
            logger.info("No service account token available in the pod. Attempting lookup from environment.");
            token = System.getenv(KUBERNETES_AUTH_TOKEN_ENV);

            if (token != null) {
                logger.info("Successfully retrieved token from environment.");
            }
        }

        return token;
    }
}
