# =========================
# Build stage (Maven + JDK 25)
# =========================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -q -DskipTests clean package


# =========================
# Runtime stage (JRE 25)
# =========================
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=build /app/target/*.jar /app/app.jar

USER app
EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
