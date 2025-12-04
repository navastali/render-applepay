
# Stage 1: Build frontend
FROM node:16 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend with Maven (Java 8)
FROM maven:3.6.3-jdk-8 AS backend-build
WORKDIR /app
# Copy backend pom and sources
COPY backend/pom.xml ./
COPY backend/src ./src
# Copy built frontend from previous stage into backend static resources
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static
# Build the Spring Boot app
RUN mvn -f ./pom.xml -B -DskipTests package

# Stage 3: Runtime image (smaller)
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
ENV PORT 8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar /app/app.jar"] 
