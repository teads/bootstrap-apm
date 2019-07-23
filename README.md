# bootstrap-apm

## Datadog

### Publish the image locally

```
sbt datadog-testing/docker:publishLocal
```

### Start the docker container running the service

```
docker run \
    --rm \
    --net host \
    -e DD_SERVICE_NAME="datadog-testing" \
    --name datadog-testing \
    datadog-testing:1.0.0
```

### Start the datadog watcher docker container

Replace [DATADOG_API_KEY] by your personal key

```
docker run \
    --rm \
    -v /var/run/docker.sock:/var/run/docker.sock:ro \
    -v /proc/:/host/proc/:ro \
    -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
    --net host \
    --cap-add NET_ADMIN \
    --pid host \
    -e DD_TAGS="env:datadog-testing" \
    -e DD_APM_ENABLED=true \
    -e DD_BIND_HOST="0.0.0.0" \
    -e DD_APM_NON_LOCAL_TRAFFIC=true \
    -e DD_LOGS_ENABLED=true \
    -e DD_API_KEY=[DATADOG_API_KEY] \
    --name datadog \
    datadog/agent:latest
```

### Query the service to generate traces

As we use host networking to simulate production usage, you should curl from inside a container.
 You can use the following command

```
docker run --net host --rm byrnedo/alpine-curl localhost:8080/test
```
