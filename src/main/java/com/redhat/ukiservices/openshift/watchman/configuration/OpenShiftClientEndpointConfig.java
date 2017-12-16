package com.redhat.ukiservices.openshift.watchman.configuration;

import javax.websocket.ClientEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpenShiftClientEndpointConfig extends ClientEndpointConfig.Configurator {

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("Authorization", Arrays.asList("Bearer 2wdr4QFgDTHZqh_eb9waIrPeYUS7IBhJ3QOwS7qhZmY"));
    }
}
