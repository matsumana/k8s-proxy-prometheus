FROM ubuntu:bionic-20191010 as builder

WORKDIR /tmp
RUN apt update && \
    apt install -y curl && \
    curl -L -O https://download.bell-sw.com/java/13.0.1/bellsoft-jdk13.0.1-linux-amd64.deb && \
    apt install -y -f ./bellsoft-jdk13.0.1-linux-amd64.deb
RUN jlink \
    --compress=2 \
    --add-modules=java.base,jdk.unsupported,java.xml,java.desktop,jdk.management,jdk.management.agent,jdk.jfr \
    --output=jre

# --------------------------------
FROM ubuntu:bionic-20191010

COPY --from=builder /tmp/jre /root/jre
ADD ./build/libs/*.jar /root/app.jar
ADD ./docker-entrypoint.sh /root/docker-entrypoint.sh

ENV JAVA_HOME "/root/jre"
ENV PATH "$JAVA_HOME/bin:$PATH"

ENTRYPOINT ["/root/docker-entrypoint.sh"]
CMD ["java", \
     "-XX:+UseG1GC", \
     "-Djava.rmi.server.hostname=127.0.0.1", \
     "-Dcom.sun.management.jmxremote", \
     "-Dcom.sun.management.jmxremote.rmi.port=8686", \
     "-Dcom.sun.management.jmxremote.port=8686", \
     "-Dcom.sun.management.jmxremote.local.only=false", \
     "-Dcom.sun.management.jmxremote.ssl=false", \
     "-Dcom.sun.management.jmxremote.authenticate=false", \
     "-Xlog:gc*=debug:/root/gc_%t_%p.log:time,level,tags:filesize=1024m,filecount=5", \
     "-XX:StartFlightRecording=name=on_startup,filename=/root/flight_recording.jfr,dumponexit=true,delay=2m,maxsize=512m", \
     "-Xms1g", \
     "-Xmx1g", \
     "-jar", \
     "/root/app.jar"]

EXPOSE 8080
