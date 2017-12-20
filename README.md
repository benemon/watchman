# Watchman

### A PoC Java Websockets client to watch for changes on the OpenShift API

#### Setup

* Install either Minishift / Red Hat Container Developer Toolkit / OpenShift Container Platform / OpenShift Origin

* Use this [brilliant little tool](https://github.com/escline/InstallCert) to extract the self-signed cert for your OpenShift environment and create a truststore

* Create a secret from this truststore in your OpenShift project: `oc create secret generic truststore --from-file=jssecacerts`

* Clone this repo: `git clone https://github.com/benemon/watchman.git`

* Build and run: `mvn clean package fabric8:deploy -Popenshift`. Deployment will fail unless you have a pre-existing SA with cluster-reader rights.

* After build is complete, assign cluster-reader permissions to the SA if you want to read the state of resources across the entire cluster: `oc adm policy add-cluster-role-to-user cluster-reader system:serviceaccount:<project>:watchman -n <project>`

* Delete the existing Pod / redeploy. The new permissions on the SA should be picked up and used.

#### Configuration

Configuration is achieved through Environment Variables, which are all fairly self-explanatory:

* OPENSHIFT_WEBSOCKET_URI - The URI to connect to. Outside OpenShift, this defaults to wss://192.168.99.100:8443/oapi/v1/deploymentconfigs?watch=true. Inside, it defaults to wss://172.30.0.1:443/oapi/v1/deploymentconfigs?watch=true

* OPENSHIFT_WEBSOCKET_TIMEOUT - Time in millseconds before the websocket connection is dropped, and the program exits. Defaults to 600000ms (10 minutes).

* OPENSHIFT_WEBSOCKET_TOKEN - When outside OpenShift, allows a Bearer token to be presented for the purposes of authentication against the OpenShift API.


#### ToDo

* Format the output nicely and dump it into something Grafana can parse
* Add Rules to interpret the conditions of the deployment
