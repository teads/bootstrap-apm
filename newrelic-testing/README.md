# NewRelic

## Publish the image locally

Replace `[NEWRELIC_KEY]` by your personal key

```
export NEWRELIC_LICENSE=[NEWRELIC_KEY]
sbt newrelic-testing/docker:publishLocal
```

## Start the docker container running the service

```
docker run \
    --rm \
    --net host \
    --name newrelic-testing \
    newrelic-testing:1.0.0
```

## Query the service to generate traces

As we use host networking to simulate production usage, you should curl from inside a container.
 You can use the following command

```
docker run --net host --rm byrnedo/alpine-curl localhost:8080/test
```
