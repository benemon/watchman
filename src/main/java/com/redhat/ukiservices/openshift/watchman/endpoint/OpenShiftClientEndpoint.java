package com.redhat.ukiservices.openshift.watchman.endpoint;

import com.redhat.ukiservices.openshift.watchman.handler.OpenShiftDeploymentConfigMessageHandler;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenShiftClientEndpoint extends Endpoint {

    private static final Logger logger = Logger.getLogger(OpenShiftClientEndpoint.class.getName());

    private Session session;
    private final long timeout;

    public OpenShiftClientEndpoint(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        logger.log(Level.INFO, String.format("Connected to endpoint: %s", session.getBasicRemote()));

        this.session = session;
        this.session.setMaxIdleTimeout(timeout);
        this.session.addMessageHandler(new OpenShiftDeploymentConfigMessageHandler());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (this.session != null) {
            try {
                this.session.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        logger.log(Level.INFO, String.format("Disconnected from endpoint: %s", session.getBasicRemote()));
    }

    @Override
    public void onError(Session session, Throwable thr) {
        if (this.session != null) {
            try {
                this.session.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        logger.log(Level.SEVERE, thr.getMessage(), thr);
    }


}
