package com.redhat.ukiservices.openshift.watchman.watch.handler;

import com.redhat.ukiservices.openshift.watchman.model.ConditionTypeEnum;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

public class OpenShiftDeploymentConfigMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftDeploymentConfigMessageParser.class);

    public static Optional<JsonObject> parseMessage(JsonObject action) {
        Optional<JsonObject> result = Optional.empty();
        String type = action.getString("type");
        JsonObject dc = action.getJsonObject("object");
        JsonObject metadata = dc.getJsonObject("metadata");
        JsonObject status = dc.getJsonObject("status");
        JsonArray conditions = status.getJsonArray("conditions");

        Integer availableReplicas = status.getInteger("availableReplicas");
        Integer unavailableReplicas = status.getInteger("unavailableReplicas");
        Integer requiredReplicas = status.getInteger("replicas");

        for (Object cObj : conditions) {

            if (cObj instanceof JsonObject) {
                JsonObject condition = (JsonObject) cObj;
                String cType = condition.getString("type");
                String cMessage = condition.getString("message");
                String cTimestamp = condition.getString("lastUpdateTime");

                // This is dumb, but it seems the received value of 'True' or 'False' is set as a String rather than a Boolean
                Boolean cStatus = Boolean.parseBoolean(condition.getString("status"));

                if (cType.equalsIgnoreCase(ConditionTypeEnum.AVAILABLE.toString())) {
                    if (cStatus) {
                        logger.debug(String.format("Project: %s - DeploymentConfig %s is READY - %d out of %d replicas available. %n Message: %s",
                                metadata.getString("namespace"),
                                metadata.getString("name"),
                                availableReplicas,
                                requiredReplicas,
                                cMessage));


                    } else {
                        logger.debug(String.format("Project %s - DeploymentConfig %s is NOT READY - %d out of %d replicas available. %n Message: %s",
                                metadata.getString("namespace"),
                                metadata.getString("name"),
                                availableReplicas,
                                requiredReplicas,
                                cMessage));
                    }

                    result = Optional.of(constructStateObject(metadata.getString("name"),
                            metadata.getString("namespace"),
                            getTimestampFromString(cTimestamp),
                            cStatus,
                            availableReplicas,
                            unavailableReplicas,
                            requiredReplicas,
                            cMessage));
                }
            }


        }

        return result;
    }

    /**
     * Construct a sensible representation of this update
     *
     * @param name                the DC name
     * @param namespace           the OpenShift project
     * @param timestamp           the time the state update happened in OpenShift
     * @param status              the status
     * @param availableReplicas   number of available replicas
     * @param unavailableReplicas number of unavailable replicas
     * @param requiredReplicas    number of required replicas
     * @param message             message pushed from OpenShift
     * @return
     */
    private static JsonObject constructStateObject(String name,
                                                   String namespace,
                                                   Long timestamp,
                                                   Boolean status,
                                                   Integer availableReplicas,
                                                   Integer unavailableReplicas,
                                                   Integer requiredReplicas,
                                                   String message) {
        JsonObject state = new JsonObject();
        state.put("project", namespace);
        state.put("name", name);
        state.put("status", status);
        state.put("message", message);
        state.put("timestamp", timestamp);

        JsonObject replicas = new JsonObject();
        replicas.put("available", availableReplicas);
        replicas.put("unavailable", unavailableReplicas);
        replicas.put("required", requiredReplicas);
        state.put("replicas", replicas);

        return state;
    }

    private static long getTimestampFromString(String dateString) {
        long timestamp;

        logger.debug("Input:" + dateString);

        Instant date = Instant.parse(dateString);
        timestamp = date.getEpochSecond();

        logger.debug("Output:" + timestamp);

        return timestamp;
    }
}
