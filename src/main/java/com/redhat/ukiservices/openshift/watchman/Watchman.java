package com.redhat.ukiservices.openshift.watchman;

import com.redhat.ukiservices.openshift.watchman.configuration.OpenShiftClientEndpointConfig;
import com.redhat.ukiservices.openshift.watchman.endpoint.OpenShiftClientEndpoint;
import io.undertow.websockets.jsr.DefaultWebSocketClientSslProvider;

import javax.net.ssl.SSLContext;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Watchman {
    private static final Logger logger = Logger.getLogger(Watchman.class.getName());

    private static final String OPENSHIFT_WEBSOCKET_URI_ENV = "OPENSHIFT_WEBSOCKET_URI";
    private static final String OPENSHIFT_WEBSOCKET_TIMEOUT_ENV = "OPENSHIFT_WEBSOCKET_TIMEOUT";
    private static final String OPENSHIFT_WEBSOCKET_TOKEN_ENV = "OPENSHIFT_WEBSOCKET_TOKEN";

    private static final String OPENSHIFT_WEBSOCKET_URI_DEFAULT = "wss://192.168.99.100:8443/oapi/v1/deploymentconfigs?watch=true";
    private static final String OPENSHIFT_WEBSOCKET_TIMEOUT_DEFAULT = "60000";

    public static void main(String[] args) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            String uri = System.getenv(OPENSHIFT_WEBSOCKET_URI_ENV) != null ? System.getenv(OPENSHIFT_WEBSOCKET_URI_ENV) : OPENSHIFT_WEBSOCKET_URI_DEFAULT;
            Long timeout = Long.parseLong(System.getenv(OPENSHIFT_WEBSOCKET_TIMEOUT_ENV) != null ? System.getenv(OPENSHIFT_WEBSOCKET_TIMEOUT_ENV) : OPENSHIFT_WEBSOCKET_TIMEOUT_DEFAULT);
            String token = System.getenv(OPENSHIFT_WEBSOCKET_TOKEN_ENV);

            logger.info(String.format("Connecting to %s ", uri));

            // Create a custom Configuration object to hold our Bearer token
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(new OpenShiftClientEndpointConfig(token)).build();

            // Set the SSL context after adding the OpenShift certificate to the java default truststore / using a custom truststore
            SSLContext sslContext = SSLContext.getDefault();
            config.getUserProperties().put(DefaultWebSocketClientSslProvider.SSL_CONTEXT, sslContext);

            Session session = container.connectToServer(new OpenShiftClientEndpoint(timeout), config, URI.create(uri));
            while (session.isOpen()) {
            }

            if (!session.isOpen()) {
                logger.log(Level.WARNING, String.format("Session timed out after %dms", session.getMaxIdleTimeout()));
            }

        } catch (DeploymentException | IOException | NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
