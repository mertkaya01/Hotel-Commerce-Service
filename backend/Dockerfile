# --- 1. asama: build (Maven ile jar uret) ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# once sadece pom + wrapper kopyala -> bagimliliklar cache'lenir, kod degisince tekrar cekilmez
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

# --- 2. asama: runtime (sadece JRE + jar) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
