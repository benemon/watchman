package com.redhat.ukiservices.openshift.watchman.endpoint;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenShiftClientEndpoint extends Endpoint {

    private static Logger logger = Logger.getLogger(OpenShiftClientEndpoint.class.getName());

    private Session session;

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        logger.log(Level.INFO, String.format("Connected to endpoint: %s", session.getBasicRemote()));

        this.session = session;
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
