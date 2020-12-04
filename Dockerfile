FROM openjdk:15-jdk as builder
WORKDIR /etc/melijn-monitorinflux
COPY ./ ./
USER root
RUN chmod +x ./gradlew
RUN ./gradlew shadowJar

FROM openjdk:15-jdk
WORKDIR /opt/melijn_monitorinflux
COPY --from=builder ./etc/melijn-monitorinflux/build/libs/ .
ENTRYPOINT java \
    -Xmx50M \
    -jar \
    ./stats.jar