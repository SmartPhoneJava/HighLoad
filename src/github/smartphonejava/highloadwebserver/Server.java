package github.smartphonejava.highloadwebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    private static final String DEFAULT_FILES_DIR = "";
    private static final int BUFFER_SIZE = 1024;

    WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            //final String dir = System.getProperty("user.dir");
            //FileOutputStream output=new FileOutputStream(dir+"/log.txt");

            String header = readHeader(input);
            System.out.println("header:"+header);

            String method = getMethodFromHeader(header);

            if (method == null) {
                send(output, 403, null, 0);
            } else if (method.equals("GET")) {
                String url = getURIFromHeader(header);
                sendFile(url, output, false);
            } else if (method.equals("HEAD")) {
                String url = getURIFromHeader(header);
                sendFile(url, output, true);
            } else {
                send(output, 405, null, 0);
            }

            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

        private String readHeader(InputStream input) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String ln = null;
            while (true) {
                ln = reader.readLine();
                if (ln == null || ln.isEmpty()) {
                    break;
                }
                builder.append(ln).append(System.getProperty("line.separator"));
            }
            return builder.toString();
        }

    private String getURIFromHeader(String header) {
        int from = header.indexOf(" ") + 1;
        if (from == 0) {
            return "/index.html";
        }
        int to = header.indexOf(" ", from);
        if (to == -1) {
            return "/index.html";
        }
        String uri = header.substring(from, to);
        uri = java.net.URLDecoder.decode(uri, StandardCharsets.UTF_8);
        if (uri.lastIndexOf("/")== uri.length()-1) {
            return uri+"index.html";
        }
        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1) {
            uri = uri.substring(0, paramIndex);
        }
        return DEFAULT_FILES_DIR + uri;
    }

    private String getMethodFromHeader(String header) {
        int to = header.indexOf(" ");
        if (to == -1) {
            return null;
        }
        return header.substring(0,to);
    }

    private void sendFile(String url, OutputStream out, Boolean isHead) {
        final String dir = System.getProperty("user.dir");
        int code;
        url = dir + url;
        if (isURLDangerous(url)) {
            code = 403;
            send(out, code, null, 0);
            return;
        }
        String mime = null;
        int size = 0;
        //Boolean isText = false;
        try {
            File file = new File(url);

            code = 200;
            mime = getContentType(file);
            size = (int)file.length();

            FileInputStream fin = null;
            if (!isHead) {
                fin = new FileInputStream(file);
                size = fin.available();
                //isText = (mime.indexOf("text") != -1);
                //if (isText)
                //    mime += "; charset=utf-8";
            }

            send(out, code, mime, size);
            if (!isHead) {
                int count;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = fin.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                fin.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            code = url.contains("/index.html") ? 403 : 404;
        }
        if (code != 200)
            send(out, code, mime, size);
    }

    private void send(OutputStream out, int code, String mime, int size) {
        String header = getHeader(code, mime, size);
        System.out.println("answer header:"+ header);
        PrintStream answer = new PrintStream(out, true, StandardCharsets.UTF_8);
        answer.print(header);
    }

    private final static char CR  = (char) 0x0D;
    private final static char LF  = (char) 0x0A;

    private final static String CRLF  = "" + CR + LF;

    private String getHeader(int code, String contentType, int conLength) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("HTTP/1.1 " + code + " " + getAnswer(code) + CRLF);

        buffer.append("Server: " + "Java web-server"+ CRLF);
        buffer.append("Connection: " + "close"+ CRLF);
        buffer.append("Date: " + new Date() + CRLF);
        buffer.append("Accept-Ranges: none "+CRLF);
        if (code == 200) {
            if (contentType != null)
                buffer.append("Content-Type: ").append(contentType).append(CRLF);
            if (conLength != 0)
                buffer.append("Content-Length: ").append(conLength).append(CRLF);
        }
        buffer.append(CRLF);

        return buffer.toString();
    }

    private String getContentType(File file) throws IOException {
        Path path = file.toPath();
        return Files.probeContentType(path);
    }

    private String getAnswer(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            default:
                return "Internal Server Error";
        }
    }

    private int subStrInStr(String origin, String subStr) {
        int count = 0;
        while (origin.contains(subStr)){
            origin = origin.replaceFirst(subStr, "");
            count++;
        }
        return count ;
    }
    private Boolean isURLDangerous(String url) {
        int nesting = 0;
        int backnesting = 0;
        backnesting = subStrInStr(url, "/..");
        if (backnesting > 0) {
            nesting = subStrInStr(url, "/") - 2 * backnesting;
            return nesting < 0;
        }
        return false;
    }

    private Boolean isFilenameAbsent(String url) {
        return url.contains("..");
    }
}

public class Server implements Runnable {

    private int serverPort = 8080;
    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private Thread runningThread = null;
    private ExecutorService threadPool =
            Executors.newFixedThreadPool(10);

    public Server(int port) {
        this.serverPort = port;
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }

            this.threadPool.execute(
                    new WorkerRunnable(clientSocket, "Thread Pooled Server"));

        }

        System.out.println("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Port " + this.serverPort + " is blocked.");
            System.exit(-1);
        }
    }
}

// 393 -> 380 -> 350 -> 314
