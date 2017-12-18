package com.redhat.ukiservices.openshift.watchman.common;

public interface CommonConstants {

    static final String OPENSHIFT_WEBSOCKET_URI_ENV = "OPENSHIFT_WEBSOCKET_URI";
    static final String OPENSHIFT_WEBSOCKET_TIMEOUT_ENV = "OPENSHIFT_WEBSOCKET_TIMEOUT";
    static final String OPENSHIFT_WEBSOCKET_TOKEN_ENV = "OPENSHIFT_WEBSOCKET_TOKEN";

    static final String OPENSHIFT_WEBSOCKET_URI_DEFAULT = "wss://192.168.99.100:8443/oapi/v1/deploymentconfigs?watch=true";
    static final String OPENSHIFT_WEBSOCKET_TIMEOUT_DEFAULT = "60000";
    static final String DEFAULT_TOKEN_PATH = "/run/secrets/kubernetes.io/serviceaccount/token";
}
