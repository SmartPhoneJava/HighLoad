import github.smartphonejava.highloadwebserver.*;


public class Main {
    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server(port);
        new Thread(server).start();
        System.out.println("Start Server on "+port);
        while (true)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Stopping Server");
                server.stop();
            }
    }
}
