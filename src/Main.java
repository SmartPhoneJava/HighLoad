import github.smartphonejava.highloadwebserver.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Properties;


public class Main {
    private static final int DEFAULT_PORT = 8081;

    public static int some(int big) {
        int b = 0;
        //System.out.println("launched");
        for (int i = 0; i < 100000000; i++) {
            b = i;
            i = b;
            b = b*b*b*big;
            while (b > 0) {
                b--;
                //System.out.println("b:"+ b);
            }
        }
        return b;
    }

    public static void mainGo() {
        Thread[] threads =  new Thread[1000];
        for ( int i = 0; i < 1000; i++) {
            final int w = 1000*1000;
            threads[i] = new Thread(new Runnable() { public void run() {
                some(w);
            }});
            threads[i].run();
        }
        some(1000*1000);
    }

    public static void main(String[] args) {
        //mainGo();
        //System.out.println("done!");
        //return;
/*
        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {

                for (;;) {}
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        System.out.println("stoppp");
        //return;*/

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
