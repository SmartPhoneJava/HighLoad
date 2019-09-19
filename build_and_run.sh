#!/bin/sh
#chmod +x build_and_run.sh && ./build_and_run.sh

javac -sourcepath ./src -Xlint:deprecation -d bin src/Main.java
jar cvf bin/webserver.jar -C bin .
#java -classpath ./bin Main 80
