# 使用官方 Maven 镜像作为基础镜像进行构建
FROM maven:3.8.1-jdk-11-slim AS build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和 src 目录到工作目录
COPY pom.xml ./
COPY src ./src/

# 使用 Maven 构建项目
RUN mvn clean package -DskipTests

# 使用官方的 Java 镜像作为基础镜像运行应用
# 使用了多阶段构建：第一阶段使用 Maven 镜像构建项目，第二阶段使用 Java 镜像运行应用
# 这样，最终的镜像只包含运行应用所需的内容，而不包含构建工具和其他不必要的文件，从而减小了镜像的大小
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建的 jar 文件到当前工作目录
COPY --from=build /app/target/*.jar app.jar

# 暴露 8080 端口
EXPOSE 8080

# 运行 Spring Boot 应用
CMD ["java", "-jar", "app.jar"]

