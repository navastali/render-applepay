############################
# Stage 1: Build Frontend
############################
FROM node:16 AS frontend-build
WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

############################
# Stage 2: Build Backend
############################
FROM maven:3.6.3-jdk-8 AS backend-build
WORKDIR /app

COPY backend/ ./backend/
COPY --from=frontend-build /frontend/build ./backend/src/main/resources/static/
RUN mvn -f backend/pom.xml clean package -DskipTests

############################
# Stage 3: Runtime
############################
FROM eclipse-temurin:8-jre
WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
