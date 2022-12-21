FROM maven:3.8.6-eclipse-temurin-19 AS builder
##############
# build container to build the rabbitmq-ping-pong-client
#############
WORKDIR /tmp
COPY pom.xml /tmp/pom.xml
COPY src /tmp/src
RUN mvn clean install


FROM eclipse-temurin:19.0.1_10-jre
##############
# runtime container
#############
WORKDIR /tmp
COPY --from=builder /tmp/target ./

ENTRYPOINT ["java"]
CMD ["-jar","rabbitmq-ping-pong-client-jar-with-dependencies.jar"]