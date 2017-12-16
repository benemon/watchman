package com.redhat.ukiservices.openshift.watchman.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OpenShiftDeploymentConfigMessageHandler.class, Logger.class})
public class OpenShiftDeploymentConfigMessageHandlerTest {

    private OpenShiftDeploymentConfigMessageHandler handler;

    @Before
    public void before() throws Exception {
        handler = new OpenShiftDeploymentConfigMessageHandler();
    }

    @Test
    public void onMessageAvailable() throws Exception {

        Logger logger = mock(Logger.class);
        Whitebox.setInternalState(OpenShiftDeploymentConfigMessageHandler.class, "logger", logger);

        String data = "{\n" +
                "  \"type\":\"MODIFIED\",\n" +
                "  \"object\":{\n" +
                "    \"kind\":\"DeploymentConfig\",\n" +
                "    \"apiVersion\":\"v1\",\n" +
                "    \"metadata\":{\n" +
                "      \"name\":\"eap-app\",\n" +
                "      \"namespace\":\"unit-test\"\n" +
                "    },\n" +
                "    \"status\":{\n" +
                "      \"latestVersion\":1,\n" +
                "      \"observedGeneration\":14,\n" +
                "      \"replicas\":2,\n" +
                "      \"updatedReplicas\":2,\n" +
                "      \"availableReplicas\":2,\n" +
                "      \"unavailableReplicas\":0,\n" +
                "      \"conditions\":[\n" +
                "        {\n" +
                "          \"type\":\"Progressing\",\n" +
                "          \"status\":\"True\",\n" +
                "          \"lastUpdateTime\":\"2017-12-15T22:15:26Z\",\n" +
                "          \"lastTransitionTime\":\"2017-12-15T22:15:06Z\",\n" +
                "          \"reason\":\"NewReplicationControllerAvailable\",\n" +
                "          \"message\":\"replication controller \\\"eap-app-1\\\" successfully rolled out\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\":\"Available\",\n" +
                "          \"status\":\"True\",\n" +
                "          \"lastUpdateTime\":\"2017-12-15T22:34:43Z\",\n" +
                "          \"lastTransitionTime\":\"2017-12-15T22:34:43Z\",\n" +
                "          \"message\":\"Deployment config has minimum availability.\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"readyReplicas\":2\n" +
                "    }\n" +
                "  }\n" +
                "}";

        handler.onMessage(data);

        verify(logger).info(anyString());
        verify(logger).info(contains("Deployment config has minimum availability"));
    }

    @Test
    public void onMessageUnavailable() throws Exception {

        Logger logger = mock(Logger.class);
        Whitebox.setInternalState(OpenShiftDeploymentConfigMessageHandler.class, "logger", logger);

        String data = "{\n" +
                "  \"type\":\"MODIFIED\",\n" +
                "  \"object\":{\n" +
                "    \"kind\":\"DeploymentConfig\",\n" +
                "    \"apiVersion\":\"v1\",\n" +
                "    \"metadata\":{\n" +
                "      \"name\":\"eap-app\",\n" +
                "      \"namespace\":\"unit-test\"\n" +
                "    },\n" +
                "    \"status\":{\n" +
                "      \"latestVersion\":1,\n" +
                "      \"observedGeneration\":14,\n" +
                "      \"replicas\":2,\n" +
                "      \"updatedReplicas\":2,\n" +
                "      \"availableReplicas\":0,\n" +
                "      \"unavailableReplicas\":2,\n" +
                "      \"conditions\":[\n" +
                "        {\n" +
                "          \"type\":\"Progressing\",\n" +
                "          \"status\":\"True\",\n" +
                "          \"lastUpdateTime\":\"2017-12-15T22:15:26Z\",\n" +
                "          \"lastTransitionTime\":\"2017-12-15T22:15:06Z\",\n" +
                "          \"reason\":\"NewReplicationControllerAvailable\",\n" +
                "          \"message\":\"replication controller \\\"eap-app-2\\\" successfully rolled out\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\":\"Available\",\n" +
                "          \"status\":\"False\",\n" +
                "          \"lastUpdateTime\":\"2017-12-15T22:34:43Z\",\n" +
                "          \"lastTransitionTime\":\"2017-12-15T22:34:43Z\",\n" +
                "          \"message\":\"Deployment config does not have minimum availability.\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"readyReplicas\":2\n" +
                "    }\n" +
                "  }\n" +
                "}";

        handler.onMessage(data);

        verify(logger).log(eq(Level.WARNING), anyString());
        verify(logger).log(eq(Level.WARNING), contains("Deployment config does not have minimum availability"));
    }
}