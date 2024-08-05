FROM amazoncorretto:21
ADD target/BFPService-0.0.1-SNAPSHOT.jar Spring-docker.jar

HEALTHCHECK --interval=1m --timeout=5s --start-period=15s --retries=3 \
  CMD curl --fail --silent localhost:8080/actuator/health | jq --exit-status -n 'inputs | if has(\"status\") then .status==\"UP\" else false end' > /dev/null || exit 1
ENTRYPOINT ["java","-jar","Spring-docker.jar"]