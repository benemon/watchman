package com.redhat.ukiservices.openshift.watchman;

import com.redhat.ukiservices.openshift.watchman.configuration.OpenShiftClientEndpointConfig;
import com.redhat.ukiservices.openshift.watchman.endpoint.OpenShiftClientEndpoint;
import io.undertow.websockets.jsr.DefaultWebSocketClientSslProvider;

import javax.net.ssl.SSLContext;
import javax.websocket.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.redhat.ukiservices.openshift.watchman.common.CommonConstants.*;

public class Watchman {
    private static final Logger logger = Logger.getLogger(Watchman.class.getName());

    private String uri;
    private Long timeout;

    public Watchman() {
        this.uri = System.getenv(OPENSHIFT_WEBSOCKET_URI_ENV) != null ? System.getenv(OPENSHIFT_WEBSOCKET_URI_ENV) : OPENSHIFT_WEBSOCKET_URI_DEFAULT;
        this.timeout = Long.parseLong(System.getenv(OPENSHIFT_WEBSOCKET_TIMEOUT_ENV) != null ? System.getenv(OPENSHIFT_WEBSOCKET_TIMEOUT_ENV) : OPENSHIFT_WEBSOCKET_TIMEOUT_DEFAULT);
    }

    public void watch() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            logger.info(String.format("Connecting to %s ", uri));

            // Create a custom Configuration object to hold our Bearer token
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(new OpenShiftClientEndpointConfig(getAuthToken())).build();

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
                logger.log(Level.WARNING, String.format("Error reading service account token %s.", e.getMessage()));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, String.format("Error closing resources %s].", e.getMessage()));
                    }
                }

            }
        } else {
            logger.log(Level.INFO, "No service account token available in the pod. Attempting lookup from environment.");
            token = System.getenv(OPENSHIFT_WEBSOCKET_TOKEN_ENV);
        }

        return token;
    }

    public static void main(String[] args) {
        Watchman w = new Watchman();
        w.watch();
    }
}
