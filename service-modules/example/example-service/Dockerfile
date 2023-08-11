# 基础镜像（不用jre是因为arthas需要用到jdk的工具包） 如果需要安装字体需要自己制作镜像
FROM openjdk:8-jdk-alpine
# 配置环境 在build镜像时可以通过 --build-args profile=xxx 进行修改
ARG profile
ENV SPRING_PROFILES_ACTIVE=${profile}

# 解决时差8小时问题
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8080

# 复制athas到目录/tools/arthas
COPY --from=hengyunabc/arthas:3.4.5-no-jdk /opt/arthas /tools/arthas

# 复制应用并重命名
COPY target/example-service.jar /example-service.jar

# start.sh（用到arthas，需要非pid为1的运行）
RUN echo 'java -DSW_AGENT_SAMPLE=100 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/usr/local/fission-machine/dump/example-service -jar $JAVA_OPTS /example-service.jar' > start.sh \
             && chmod 777 start.sh

# stop.sh
RUN echo 'curl http://localhost:8080/deregisterInstance \n sleep 70' > stop.sh \
              && chmod 777 stop.sh

# 执行
CMD [ "sh", "-c", "./start.sh" ]