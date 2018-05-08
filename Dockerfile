FROM openjdk:8-jre-slim

RUN useradd -u 10001 myuser

WORKDIR /usr/src/app
COPY ./target/*.jar ./app.jar

RUN apt-get update && apt-get install -y wget
RUN wget -q -O- https://storage.googleapis.com/cloud-profiler/java/latest/profiler_java_agent.tar.gz | tar xfvz -
RUN	chown -R myuser ./app.jar *.so

USER myuser

ENTRYPOINT ["java","-Xms512m","-Xmx1024m","-Djava.security.egd=file:/dev/urandom","-agentpath:/usr/src/app/profiler_java_agent.so=-cprof_service=carts,-logtostderr","-jar","./app.jar", "--port=8080"]
#ENTRYPOINT ["java","-Xms512m","-Xmx1024m","-Djava.security.egd=file:/dev/urandom","-jar","./app.jar", "--port=8080"]