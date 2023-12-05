# 使用官方 Maven 镜像作为基础镜像进行构建
# AS build 指定了这个阶段的名称为 build，这个名称可以用来在后面的 FROM 指令引用
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build

# 设置工作目录
WORKDIR /app

# 首先仅复制pom.xml，进行依赖下载
# 这样，只要pom.xml没有变化，Maven依赖就会被缓存起来，不需要每次都重新下载
# 但我发现这样做效果并不完美，还是有一些依赖会被重新下载
COPY pom.xml ./
RUN mvn -T 1C dependency:go-offline

# 然后复制源码并编译
# -T 1C表示每个核心一个线程
# -Dmaven.test.skip=true 表示不编译test/下的代码
COPY src ./src/
RUN mvn -T 1C -Dmaven.test.skip=true clean package

# 使用官方的 Java 镜像作为基础镜像运行应用
# 使用了多阶段构建：第一阶段使用 Maven 镜像构建项目，第二阶段使用 Java 镜像运行应用
# 这样，最终的镜像只包含运行应用所需的内容，而不包含构建工具和其他不必要的文件，从而减小了镜像的大小
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建的 jar 文件到当前工作目录
COPY --from=build /app/target/*.jar app.jar

# 暴露 9090 端口
# 其实也不用写，EXPOSE 指令主要是为了文档化的目的，它告诉使用该镜像的人这个容器内部监听的端口
# 但是，EXPOSE 并不会自动在宿主主机上打开对应的端口，所以在运行时需要使用 -p 参数来指定端口映射
EXPOSE 9090

# 设置环境变量来禁用 Springdoc OpenAPI
# Spring Boot 可以从环境变量中读取配置属性，并且会自动将这些环境变量映射到相应的配置属性上
# 例如，springdoc.api-docs.enabled 这个配置属性可以通过设置一个名为 SPRINGDOC_API_DOCS_ENABLED 的环境变量来覆盖
ENV SPRINGDOC_API_DOCS_ENABLED=false

# 运行 Spring Boot 应用
CMD ["java", "-jar", "app.jar"]

