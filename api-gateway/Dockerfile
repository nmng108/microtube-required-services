# syntax=docker/dockerfile:1

# Comments are provided throughout this file to help you get started.
# If you need more help, visit the Dockerfile reference guide at
# https://docs.docker.com/go/dockerfile-reference/

# Want to help us make this template better? Share your feedback here: https://forms.gle/ybq9Krt8jtBL3iCk7

################################################################################

ARG GLOWROOT_COLLECTOR_ADDRESS="http://localhost:8181"
ARG LIBC=musl
# Create a stage for resolving and downloading dependencies.
FROM bellsoft/liberica-runtime-container:jdk-21-crac-slim-$LIBC AS deps

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

RUN java -Djarmode=tools -jar target/app.jar extract --destination extracted

################################################################################

# Create a new stage for running the application in development/local environment.
# alpine-temurin JRE is lighter than alpaquita-liberica JRE ~100MB
FROM bellsoft/liberica-runtime-container:jre-21-crac-cds-slim-$LIBC AS development
WORKDIR /opt/app

#COPY ./glowroot/ ./glowroot/
# Copy the "lib" directory & the executable jar file from the "extract" stage.
COPY --from=extract build/extracted/lib/ ./lib/
COPY --from=extract build/extracted/app.jar ./

ARG GLOWROOT_COLLECTOR_ADDRESS
ENV JAVA_TOOL_OPTIONS \
    --enable-preview \
#    -Xms512m -Xmx2g \
    -Dspring.profiles.active=dev \
    # Enable debugger
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000
#    -javaagent:glowroot/glowroot.jar \
#    -Dglowroot.collector.address=$GLOWROOT_COLLECTOR_ADDRESS

VOLUME /opt/app/logs/
EXPOSE 8080/tcp 8081/tcp 8000

#ENTRYPOINT sh
ENTRYPOINT [ "java", "-jar", "./app.jar" ]

################################################################################

# Create a custom Java runtime
FROM bellsoft/liberica-runtime-container:jdk-all-21-cds-slim-$LIBC AS custom-runtime

WORKDIR /build

#COPY --chmod=0755 mvnw mvnw
#COPY .mvn/ .mvn/
#RUN sed -i 's/\r$//' mvnw

#RUN ./mvnw dependency:build-classpath | grep "\.jar" > ./classpaths.tmp
#COPY --from=package /build/target/app.jar ./
#COPY --from=extract /build/target/extracted/dependencies/ ./
#RUN rm -f ./BOOT-INF/lib/okio-3.6.0.jar
#RUN $JAVA_HOME/bin/jdeps \
#      --print-module-deps --ignore-missing-deps \
#      --class-path "$(cat ./classpaths.tmp)" \
#      --module-path ./BOOT-INF/lib \
#      --recursive --multi-release 21 \
#      ./app.jar \
##    | awk -v RS=',' '/^java\./ {print $0}' \
##    | awk -F " " '{ printf "%s%s", (NR==1 ? "" : ","), $0 }' \
#    > ./jmodules.tmp
# Despite including all (21) jdk modules, the result image is still 100MB smaller than eclipse-temurin:21-jre-alpine-3.21
RUN $JAVA_HOME/bin/jlink \
      --add-modules ALL-MODULE-PATH \
#      --add-modules $(cat ./jmodules.tmp) \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress zip-3 \
      --output /javaruntime/

################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
# Larger than alpine image in uncompressed size
FROM bellsoft/alpaquita-linux-base:$LIBC AS production
# Copy custom JRE into this final image
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY --from=custom-runtime /javaruntime/ $JAVA_HOME

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
RUN mkdir -p /opt/app/logs && chown -R appuser /opt/app

USER appuser
WORKDIR /opt/app

#COPY ./glowroot/ ./glowroot/
# Copy lib directory & the executable jar file from the "extract" stage.
COPY --from=extract build/extracted/lib/ ./lib/
COPY --from=extract build/extracted/app.jar ./

ENV SPRING_PROFILES_ACTIVE=prod
ARG GLOWROOT_COLLECTOR_ADDRESS=http://glowroot:8181
ENV JAVA_TOOL_OPTIONS \
    --enable-preview \
    -Xms512m -Xmx2g
#    -javaagent:glowroot/glowroot.jar \
#    -Dglowroot.collector.address=$GLOWROOT_COLLECTOR_ADDRESS

VOLUME /opt/app/logs/
EXPOSE 8080/tcp 8081/tcp

ENTRYPOINT [ "java", "-jar", "./app.jar" ]
# - If using external config files (application[-<profile>].yml, bootstrap.yml)
# and those files are placed in a directory other than working dir ("./") or in "./config/" dir, add this option (mostly in production env):
#       "--spring.config.location=file:./,optional:file:./config/"
# - If using external logging config file (logback.xml), add this option (production env only):
#       "--LOGGING_CONFIG_FILEPATH=./config/logback-spring.xml
