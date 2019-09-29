
jar cvfm webserver.jar src/META-INF/MANIFEST.MF -C src/ .
taskset -c 0,1,2,3,4,5,6,7 java -classpath bin:bin/webserver.jar -server -XX:+UseTLAB -XX:+UseNUMA -Djava.compiler=jitc -Xmx128M -Xms128M -Xmn128M  Main 8080

