package com.redhat.ukiservices.openshift.watchman.configuration;

import javax.websocket.ClientEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class OpenShiftClientEndpointConfig extends ClientEndpointConfig.Configurator {

    private static Logger logger = Logger.getLogger(OpenShiftClientEndpointConfig.class.getName());

    private String token;

    public OpenShiftClientEndpointConfig(String bearerToken) {
        this.token = bearerToken;
    }

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {

        if (token != null && token.length() > 0) {
            headers.put("Authorization", Arrays.asList(String.format("Bearer %s", token)));
        } else {
            logger.warning("No Authorization token found.");
        }

    }
}
