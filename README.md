# Watchman

### A PoC Vert.x Websockets client to watch for changes on the OpenShift API

#### Setup

* Install either Minishift / Red Hat Container Developer Toolkit / OpenShift Container Platform / OpenShift Origin

* Clone this repo: `git clone https://github.com/benemon/watchman.git`

* Build and run: `mvn clean package fabric8:deploy -Popenshift`. Deployment will fail unless you have a pre-existing SA with cluster-reader rights.

* Assign cluster-reader permissions to the SA if you want to read the state of resources across the entire cluster: `oc adm policy add-cluster-role-to-user cluster-reader system:serviceaccount:<project>:watchman -n <project>`

* Delete the existing Pod / redeploy. The new permissions on the SA should be picked up and used.

#### Configuration

Configuration is achieved through Environment Variables, which are all fairly self-explanatory:

* KUBERNETES_SERVICE_HOST - The Host to connect to. Outside OpenShift, this defaults to https://192.168.99.100:8443/.

* KUBERNETES_SERVICE_PORT - The Port to connect to. Outside OpenShift, this defaults to 8443.

* KUBERNETES_AUTH_TOKEN - The Token to use for authentication. Can be either a service account, or the result of `oc whoami -t`.

**NOTE: When running inside OpenShift, these values will be set implicitly. There is no need to set them manually**

In addition, to push the data gathered to InfluxDB (for future use in Grafana Dashboards), configure the following:

* INFLUXDB_SERVICE_HOST - The InfluxDB host.

* INFLUXDB_SERVICE_PORT - The InfluxDB port.

* INFLUXDB_DATABASE - The InfluxDB Database.

Data will get pushed into a single `replicas` table. The structure will resemble this:

```
> select * from replicas
name: replicas
time                available deployment      project   required unavailable
----                --------- ----------      -------   -------- -----------
1515163280000000000 1         docker-registry default   1        0
1515163298000000000 1         router          default   1        0
1515164308000000000 1         eap-app         myproject 1        0
1515164455000000000 0         docker-registry default   1        1
1515164613000000000 1         docker-registry default   1        0
1515164614000000000 2         eap-app         myproject 3        1
1515164672000000000 1         eap-app         myproject 1        0
1515164747000000000 2         eap-app         myproject 3        1

```

If no configuration is found, the payload will still get logged, but you should see a message similar to the following in the console:

`WARNING: InfluxDB connection error. Make sure INFLUXDB_SERVICE_HOST,INFLUXDB_SERVICE_PORT, and INFLUXDB_DATABASE are set correctly`
