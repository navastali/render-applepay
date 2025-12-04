############################
# Stage 1: Build Frontend
############################
FROM node:16 AS frontend-build
WORKDIR /frontend

# Copy only package files first
COPY frontend/package*.json ./

# Install dependencies
RUN npm install --no-audit --no-fund

# Now copy the rest of the source code
COPY frontend/ ./

# Build React app
RUN npm run build


############################
# Stage 2: Build Backend
############################
FROM maven:3.6.3-jdk-8 AS backend-build
WORKDIR /app

# Copy backend source code
COPY backend/ ./backend/

# Copy frontend build output into Spring Boot static folder
COPY --from=frontend-build /frontend/build ./backend/src/main/resources/static/

# Build Spring Boot app
RUN mvn -f backend/pom.xml clean package -DskipTests


############################
# Stage 3: Runtime
############################
FROM eclipse-temurin:8-jre
WORKDIR /app

# Copy Spring Boot JAR
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
