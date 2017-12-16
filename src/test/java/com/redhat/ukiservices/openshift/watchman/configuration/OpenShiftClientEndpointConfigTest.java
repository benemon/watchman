package com.redhat.ukiservices.openshift.watchman.configuration;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OpenShiftClientEndpointConfigTest {

    @Test
    public void beforeRequest() {
        Map<String, List<String>> headers = new HashMap<>();
        OpenShiftClientEndpointConfig config = new OpenShiftClientEndpointConfig();
        config.beforeRequest(headers);

        assertEquals(headers.size(), 1);
        List<String> token = headers.get("Authorization");
        assertNotNull(token);
    }

}