package com.redhat.ukiservices.openshift.watchman;

import com.redhat.ukiservices.openshift.watchman.configuration.OpenShiftClientEndpointConfig;
import com.redhat.ukiservices.openshift.watchman.endpoint.OpenShiftClientEndpoint;
import com.redhat.ukiservices.openshift.watchman.handler.OpenShiftDeploymentConfigMessageHandler;
import io.undertow.websockets.jsr.DefaultWebSocketClientSslProvider;

import javax.net.ssl.SSLContext;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Watchman {
    private static final Logger logger = Logger.getLogger(Watchman.class.getName());

    public static void main(String[] args) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            String uri = "wss://192.168.99.100:8443/oapi/v1/deploymentconfigs?watch=true";
            logger.info("Connecting to " + uri);

            // Create a custom Configuration object to hold our Bearer token
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(new OpenShiftClientEndpointConfig()).build();

            // Set the SSL context after adding the OpenShift certificate to the java default keystore
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);

            config.getUserProperties().put(DefaultWebSocketClientSslProvider.SSL_CONTEXT, sslContext);

            Session session = container.connectToServer(new OpenShiftClientEndpoint(), config, URI.create(uri));
            session.setMaxIdleTimeout(600000);
            session.addMessageHandler(new OpenShiftDeploymentConfigMessageHandler());

            while (session.isOpen()) {
            }

            if (!session.isOpen()) {
                logger.log(Level.WARNING, String.format("Session timed out after %dms", session.getMaxIdleTimeout()));
            }

        } catch (DeploymentException | IOException | NoSuchAlgorithmException | KeyManagementException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
