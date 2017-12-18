package com.redhat.ukiservices.openshift.watchman.common;

public interface CommonConstants {

    String OPENSHIFT_WEBSOCKET_URI_ENV = "OPENSHIFT_WEBSOCKET_URI";
    String OPENSHIFT_WEBSOCKET_TIMEOUT_ENV = "OPENSHIFT_WEBSOCKET_TIMEOUT";
    String OPENSHIFT_WEBSOCKET_TOKEN_ENV = "OPENSHIFT_WEBSOCKET_TOKEN";

    String OPENSHIFT_WEBSOCKET_URI_DEFAULT = "wss://192.168.99.100:8443/oapi/v1/deploymentconfigs?watch=true";
    String OPENSHIFT_WEBSOCKET_TIMEOUT_DEFAULT = "600000";
    String DEFAULT_TOKEN_PATH = "/run/secrets/kubernetes.io/serviceaccount/token";
}
