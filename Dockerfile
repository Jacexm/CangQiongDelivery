# 多阶段构建 Dockerfile for Sky Take Out Application

# ================================
# Stage 1: Build Stage
# ================================
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom 文件（利用 Docker 缓存层）
COPY pom.xml .
COPY sky-common/pom.xml ./sky-common/
COPY sky-pojo/pom.xml ./sky-pojo/
COPY sky-server/pom.xml ./sky-server/

# 下载依赖（这一层会被缓存，除非 pom 文件改变）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY sky-common/src ./sky-common/src
COPY sky-pojo/src ./sky-pojo/src
COPY sky-server/src ./sky-server/src

# 执行打包，跳过测试
RUN mvn clean package -DskipTests -B

# ================================
# Stage 2: Runtime Stage
# ================================
FROM eclipse-temurin:17-jre

# 安装必要的工具和时区数据 (Debian-based 使用 apt-get)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    tzdata \
    curl \
    procps && \
    rm -rf /var/lib/apt/lists/*

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户（安全最佳实践，Debian 语法）
# 使用 --non-unique 或更高的 GID 来避免冲突
RUN groupadd -g 10000 skyapp && \
    useradd -r -u 10000 -g skyapp skyapp

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p /app/logs /app/config && \
    chown -R skyapp:skyapp /app

# 从构建阶段复制 jar 文件
COPY --from=builder /build/sky-server/target/*.jar /app/app.jar

# 切换到非 root 用户
USER skyapp

# 暴露应用端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heap-dump.hprof"

# 健康检查 - 检查应用是否响应
# 注意：如果应用有 /actuator/health 端点会更好
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# 启动应用
# 使用环境变量 SPRING_PROFILES_ACTIVE 来指定配置文件
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}"]

