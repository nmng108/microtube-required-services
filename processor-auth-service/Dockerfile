# syntax=docker/dockerfile:1

# Comments are provided throughout this file to help you get started.
# If you need more help, visit the Dockerfile reference guide at
# https://docs.docker.com/go/dockerfile-reference/

# Want to help us make this template better? Share your feedback here: https://forms.gle/ybq9Krt8jtBL3iCk7

################################################################################

ARG GLOWROOT_COLLECTOR_ADDRESS="http://localhost:8181"

# Create a stage for resolving and downloading dependencies.
FROM eclipse-temurin:21-jdk-alpine-3.21 AS deps

WORKDIR /build

# Copy the mvnw wrapper with executable permissions.
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
RUN sed -i 's/\r$//' mvnw

# Download dependencies as a separate step to take advantage of Docker's caching.
# Leverage a cache mount to /root/.m2 so that subsequent builds don't have to
# re-download packages.
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

################################################################################

# Create a stage for building the application based on the stage with downloaded dependencies.
# This Dockerfile is optimized for Java applications that output an uber jar, which includes
# all the dependencies needed to run your app inside a JVM. If your app doesn't output an uber
# jar and instead relies on an application server like Apache Tomcat, you'll need to update this
# stage with the correct filename of your package and update the base image of the "final" stage
# use the relevant app server, e.g., using tomcat (https://hub.docker.com/_/tomcat/) as a base image.
FROM deps AS package

WORKDIR /build

COPY ./src src/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################

# Create a stage for extracting the application into separate layers.
# Take advantage of Spring Boot's layer tools and Docker's caching by extracting
# the packaged application into separate layers that can be copied into the final stage.
# See Spring's docs for reference:
# https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html
FROM package AS extract

WORKDIR /build

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################

# Create a new stage for running the application in development (local) environment.

FROM eclipse-temurin:21-jre-alpine-3.21 AS development
RUN apk --update add ffmpeg && \
    apk cache clean && rm -rf /var/cache/apk/*

WORKDIR /build

COPY --from=extract /build/target/extracted/dependencies/ ./
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/target/extracted/application/ ./
COPY ./glowroot/ ./glowroot/

# Enable debugger
ARG GLOWROOT_COLLECTOR_ADDRESS
ENV JAVA_TOOL_OPTIONS \
    --enable-preview \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000
#    -javaagent:glowroot/glowroot.jar \
#    -Dglowroot.collector.address=$GLOWROOT_COLLECTOR_ADDRESS

EXPOSE 8080/tcp 8081/tcp 8000

ENTRYPOINT [ "java", "-Dspring.profiles.active=dev", "org.springframework.boot.loader.launch.JarLauncher" ]
CMD [ "-Xms512m", "-Xmx3g" ]
################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
FROM eclipse-temurin:21-jre-jammy AS production

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
RUN mkdir -p /opt/app/logs && chown -R appuser /opt/app/logs
USER appuser

# Copy the executable from the "package" stage.
COPY --from=extract build/target/extracted/dependencies/ ./
COPY --from=extract build/target/extracted/spring-boot-loader/ ./
COPY --from=extract build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract build/target/extracted/application/ ./
COPY glowroot/ glowroot/

ENV SPRING_PROFILES_ACTIVE=dev
ARG GLOWROOT_COLLECTOR_ADDRESS
ENV JAVA_TOOL_OPTIONS \
    --enable-preview \
    -javaagent:glowroot/glowroot.jar \
    -Dglowroot.collector.address=$GLOWROOT_COLLECTOR_ADDRESS

EXPOSE 8080/tcp 8081/tcp

ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]
CMD [ "-Xms512m", "-Xmx3g" ]
# - If using external config files (application[-<profile>].yml, bootstrap.yml)
# and those files are placed in a directory other than working dir ("./") or in "./config/" dir, add this option (mostly in production env):
#       "--spring.config.location=file:./,optional:file:./config/"
# - If using external logging config file (logback.xml), add this option (production env only):
#       "--LOGGING_CONFIG_FILEPATH=./config/logback-spring.xml