FROM centos:7

RUN yum install -y java-1.8.0-openjdk-headless \
    && yum clean all -y \
    && mkdir -p /opt/lib \
    && mkdir -p  /data/home/my-netty-gateway/config \
    && mkdir -p  /data/home/my-netty-gateway/lib

COPY target/my-netty-gateway-0.0.1-SNAPSHOT.jar /opt/lib

WORKDIR  /opt/lib

CMD ["java","-jar",\
            "-server",\
            "-Xmx300m",\
            "-Xms300m",\
            "-Xmn100m",\
            "-Dspring.config.location=/data/home/my-netty-gateway/config/application.properties",\
            "-Dlogging.config=/data/home/my-netty-gateway/config/logback.xml",\
            "/opt/lib/my-netty-gateway-0.0.1-SNAPSHOT.jar"]

