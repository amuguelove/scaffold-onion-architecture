FROM adoptopenjdk/openjdk11:jre-11.0.9_11-alpine

# 设置时区
RUN apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

COPY build/libs/*-SNAPSHOT.jar /opt/app.jar

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/opt/app.jar"]