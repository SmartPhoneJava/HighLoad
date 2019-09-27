import github.smartphonejava.highloadwebserver.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class Main {
    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File("etc/httpd.conf")));
        } catch (IOException e) {
            //e.printStackTrace();
        }

        int cpu_limit = Integer.valueOf(props.getProperty("cpu_limit", "1"));
        int thread_limit = Integer.valueOf(props.getProperty("thread_limit"));
        String document_root = props.getProperty("document_root", "1.0");


        Server server = new Server(port, cpu_limit, thread_limit, document_root);
        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
            server.shutdown();
        }});
        server.run();
    }
}
