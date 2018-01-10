package com.redhat.ukiservices.openshift.watchman.common;

public interface CommonConstants {

    String KUBERNETES_HOST_ENV = "KUBERNETES_SERVICE_HOST";
    String KUBERNETES_HOST_DEFAULT = "192.168.99.100";
    String KUBERNETES_PORT_ENV = "KUBERNETES_SERVICE_PORT";
    String KUBERNETES_PORT_DEFAULT = "8443";

    String OPENSHIFT_API_ENDPOINT = "/oapi/v1/deploymentconfigs?watch=true";


    String KUBERNETES_AUTH_TOKEN_ENV = "KUBERNETES_AUTH_TOKEN";
    String DEFAULT_TOKEN_PATH = "/run/secrets/kubernetes.io/serviceaccount/token";

    String INFLUXDB_PERSISTENCE_ADDDRESS = "influxdb.persist";
    String INFLUXDB_SERVICE_HOST_ENV = "INFLUXDB_SERVICE_HOST";
    String INFLUXDB_SERVICE_PORT_ENV = "INFLUXDB_SERVICE_PORT";
    String INFLUXDB_DATABASE_ENV = "INFLUXDB_DATABASE";
    String EMPTY_STRING = "";
}
