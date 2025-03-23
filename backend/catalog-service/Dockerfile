FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . /app
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Configurar usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"] 