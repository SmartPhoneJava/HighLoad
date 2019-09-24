FROM openjdk:11
WORKDIR /webserver
COPY . .
ENV JAVA_OPTS="-Duser.timezone=GMT -Dfile.encoding=UTF-8 -Denvironment.type=production"

#CMD exec java $JAVA_OPTS -jar out/artifacts/web_server_jar/web_server.jar
#CMD exec chmod +x build_and_run.sh && ./build_and_run.sh
#javac -sourcepath ./src -Xlint:deprecation -d bin src/Main.java
#CMD exec chmod +x build_and_run.sh -D FOREGROUND
#java -classpath ./bin Main 80

#RUN javac -sourcepath ./src -Xlint:deprecation -d bin src/Main.java
#RUN jar cvf bin/webserver.jar -C bin .
#RUN jar cmvf bin/META-INF/MANIFEST.MF webserver.jar  <files to include>

RUN javac -sourcepath src -d bin -classpath bin/webserver.jar src/Main.java
#RUN java -classpath bin:bin/webserver.jar src/Main.java

CMD exec java $JAVA_OPTS -classpath bin:bin/webserver.jar Main 80

#CMD exec java $JAVA_OPTS -jar bin/webserver.jar
