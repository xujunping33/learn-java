#
# Day 186：Dockerfile（multi-stage）
#

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
# Day192：Compose healthcheck 需要容器内 curl
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar

ENV SERVER_PORT=8081
EXPOSE 8081

# 默认走 docker profile，数据源与 Redis 全走环境变量/compose 服务名
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","/app/app.jar"]

