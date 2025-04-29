FROM eclipse-temurin:21-jdk-alpine-3.21 AS deps

WORKDIR /build

COPY --chmod=0755 ./gradlew ./gradlew
COPY ./gradle/ ./gradle/
COPY settings.gradle ./
RUN --mount=type=bind,source=build.gradle,target=build.gradle \
    --mount=type=cache,target=/root/.gradle/ \
    --mount=type=cache,target=./.gradle/ \
    ./gradlew --no-daemon --build-cache --gradle-user-home /root/.gradle --project-cache-dir ./.gradle/ dependencies --quiet

FROM deps AS package

WORKDIR /build

COPY ./src/ ./src/
RUN --mount=type=bind,source=build.gradle,target=build.gradle \
    --mount=type=cache,target=/root/.gradle/ \
    --mount=type=cache,target=./.gradle/ \
    ./gradlew --no-daemon --build-cache --gradle-user-home /root/.gradle --project-cache-dir ./.gradle/ build \
    && mv ./build/libs/$(prop="name"; ./gradlew properties | grep "^$prop: " | sed "s/^$prop: //")-$(prop="version"; ./gradlew properties | grep "^$prop: " | sed "s/^$prop: //").jar ./app.jar

FROM package AS extract

WORKDIR /build

RUN java -Djarmode=tools -jar app.jar extract --destination extracted

FROM eclipse-temurin:21-jdk-alpine-3.21 AS custom-runtime

RUN $JAVA_HOME/bin/jlink \
      --add-modules ALL-MODULE-PATH \
#      --add-modules $(cat ./jmodules.tmp) \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress zip-3 \
      --output /javaruntime/


FROM alpine:3.21 AS production
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY --from=custom-runtime /javaruntime/ $JAVA_HOME

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

COPY --from=extract /build/extracted/lib/ ./lib/
COPY --from=extract /build/extracted/app.jar ./app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS -Xms512m -Xmx1g

ENTRYPOINT [ "java", "-jar", "app.jar" ]
