FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
WORKDIR /app

RUN groupadd --system meca && useradd --system --gid meca --home-dir /app --shell /usr/sbin/nologin meca
COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R meca:meca /app

USER meca
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
