FROM openjdk:11
WORKDIR /webserver
COPY . .
ENV JAVA_OPTS="-Duser.timezone=GMT -Dfile.encoding=UTF-8 -Denvironment.type=production"

RUN javac -sourcepath src -d bin -classpath bin/webserver.jar src/Main.java
CMD exec java $JAVA_OPTS -classpath bin:bin/webserver.jar Main 80
