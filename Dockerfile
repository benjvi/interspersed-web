FROM benjvi/interspersedweb-buildcache AS BUILD_IMAGE
ENV APP_HOME=/root/dev/intersperse
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew gradlew.bat $APP_HOME/
COPY src $APP_HOME/src
COPY gradle $APP_HOME/gradle
# download dependencies
RUN ./gradlew build
FROM openjdk:8-jre
RUN apt-get update && apt-get install -y ffmpeg python3-dev python3-pip
RUN pip3 install pydub
WORKDIR /root/
COPY --from=BUILD_IMAGE /root/dev/intersperse/build/libs/interspersed-web-0.0.1-SNAPSHOT.jar .
COPY interleaved /root/interleaved
EXPOSE 8080
CMD ["java","-jar","interspersed-web-0.0.1-SNAPSHOT.jar"]