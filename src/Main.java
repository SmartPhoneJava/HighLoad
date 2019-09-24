import github.smartphonejava.highloadwebserver.*;


public class Main {
    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
            server.shutdown();
        }});
        server.run();
    }
}
