package com.redhat.ukiservices.openshift.watchman.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.ukiservices.openshift.watchman.endpoint.OpenShiftClientEndpoint;
import com.redhat.ukiservices.openshift.watchman.model.ConditionTypeEnum;

import javax.websocket.MessageHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenShiftDeploymentConfigMessageHandler implements MessageHandler.Whole<String> {

    private static Logger logger = Logger.getLogger(OpenShiftClientEndpoint.class.getName());

    private JsonParser jParser;

    public OpenShiftDeploymentConfigMessageHandler() {
        jParser = new JsonParser();
    }

    @Override
    public void onMessage(String message) {
        JsonElement action = jParser.parse(message);
        processMessage(action.getAsJsonObject());
    }

    private void processMessage(JsonObject action) {
        String type = action.get("type").getAsString();
        JsonObject dc = action.getAsJsonObject("object");
        JsonObject metadata = dc.getAsJsonObject("metadata");
        JsonObject status = dc.getAsJsonObject("status");
        JsonArray conditions = status.getAsJsonArray("conditions");

        Integer availableReplicas = status.get("availableReplicas").getAsInt();
        Integer unavailableReplicas = status.get("unavailableReplicas").getAsInt();
        Integer requiredReplicas = status.get("replicas").getAsInt();

        for (JsonElement condition : conditions) {
            String cType = condition.getAsJsonObject().get("type").getAsString();
            String cMessage = condition.getAsJsonObject().get("message").getAsString();
            Boolean cStatus = condition.getAsJsonObject().get("status").getAsBoolean();

            if (cType.equalsIgnoreCase(ConditionTypeEnum.AVAILABLE.toString())) {
                if (cStatus) {
                    logger.info(String.format("DeploymentConfig %s is READY - %d out of %d replicas available. %n Message: %s",
                            metadata.get("name").getAsString(),
                            availableReplicas,
                            requiredReplicas,
                            cMessage));
                } else {
                    logger.log(Level.WARNING, String.format("DeploymentConfig %s is NOT READY - %d out of %d replicas available. %n Message: %s",
                            metadata.get("name").getAsString(),
                            availableReplicas,
                            requiredReplicas,
                            cMessage));
                }
            }
        }
    }
}
