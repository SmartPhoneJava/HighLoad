package github.smartphonejava.highloadwebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    private static final String DEFAULT_FILES_DIR = System.getProperty("user.dir");
    private static final int BUFFER_SIZE = 1024;
    private final static char CR  = (char) 0x0D;
    private final static char LF  = (char) 0x0A;
    private final static String CRLF  = "" + CR + LF;

    WorkerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();

            String readRequest = readRequest(input);
            String method = getRequestMethod(readRequest);

            switch (method) {
                case "GET": {
                    String url = getRequstURL(readRequest);
                    sendFile(url, output, false);
                    break;
                }
                case "HEAD": {
                    String url = getRequstURL(readRequest);
                    sendFile(url, output, true);
                    break;
                }
                default:
                    writeResponseHeader(output, 405, null, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                if (output != null)
                    e.printStackTrace();
            }
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Получить заголовок запроса
     * @param input поток ввода
     * @return строка, содержащая содержимое запроса
     * @throws IOException
     */
    private String readRequest(InputStream input) throws IOException {
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

    /**
     * Получить путь до файла
     * @param header заголовок запроса
     * @return null если путь до фалйа небезопасен, иначе строка с путем до файла
     */
    private String getRequstURL(String header) {
        int from = header.indexOf(" ") + 1;
        if (from == 0)
            return DEFAULT_FILES_DIR+"/index.html";

        int to = header.indexOf(" ", from);
        if (to == -1)
            return DEFAULT_FILES_DIR+"/index.html";

        String uri = header.substring(from, to);
        uri = java.net.URLDecoder.decode(uri, StandardCharsets.UTF_8);
        if (uri.lastIndexOf("/")== uri.length()-1)
            return DEFAULT_FILES_DIR + uri+"index.html";

        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1)
            uri = uri.substring(0, paramIndex);

        if (isURLDangerous(uri))
            return null;

        return DEFAULT_FILES_DIR + uri;
    }

    /**
     * Получить метод запроса
     * @param header строка с заголовком запроса
     * @return строка с методом
     */
    private static String getRequestMethod(String header) {
        int to = header.indexOf(" ");
        if (to == -1) {
            return null;
        }
        return header.substring(0,to);
    }

    /**
     * Отправить ответ с телом
     * @param url путь до файла
     * @param out поток вывода
     * @param isHead флаг является ли метод запроса HEAD
     */
    private void sendFile(String url, OutputStream out, Boolean isHead) {
        if (url == null) {
            writeResponseHeader(out, 403, null, 0);
            return;
        }
        int code = 200;
        String mime = null;
        int size = 0;
        //Boolean isText = false;
        try {
            File file = new File(url);
            mime = getContentType(file);
            size = (int)file.length();
            FileInputStream fin = new FileInputStream(file);
            //isText = (mime.indexOf("text") != -1);
            //if (isText)
            //    mime += "; charset=utf-8";
            writeResponseHeader(out, code, mime, size);

            if (!isHead) {
                int count;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = fin.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            }
            fin.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            code = url.contains("/index.html") ? 403 : 404;
        }
        if (code != 200)
            writeResponseHeader(out, code, mime, size);
    }

    /**
     * Записать результаты выполнения запроса в заголовки
     * @param out поток вывода
     * @param code http код ошибки
     * @param mime тип файла
     * @param size размер файла
     */
    private void writeResponseHeader(OutputStream out, int code, String mime, int size) {
        String header = createResponseHeader(code, mime, size);
        PrintStream answer = new PrintStream(out, true, StandardCharsets.UTF_8);
        answer.print(header);
    }

    /**
     * Поместить информацию в заголовок ответа
     * @param code код шибки
     * @param contentType тип файла
     * @param contentLength размер файла
     * @return строка с заголовком
     */
    private String createResponseHeader(int code, String contentType, int contentLength) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("HTTP/1.1 " + code + " " + getAnswer(code) + CRLF);
        buffer.append("Server: " + "Java web-server" + CRLF);
        buffer.append("Connection: " + "close" + CRLF);
        buffer.append("Date: " + new Date() + CRLF);
        buffer.append("Accept-Ranges: none " + CRLF);
        if (code == 200) {
            if (contentType != null)
                buffer.append("Content-Type: ").append(contentType).append(CRLF);
            if (contentLength != 0)
                buffer.append("Content-Length: ").append(contentLength).append(CRLF);
        }
        buffer.append(CRLF);
        return buffer.toString();
    }

    /**
     * Получить тип файла
     * @param file файл
     * @return тип файла
     * @throws IOException
     */
    private String getContentType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }

    /**
     * Получить комменатрией к коду ошибки
     * @param code код ошибки
     * @return комментарий
     */
    private String getAnswer(int code) {
        switch (code) {
            case 200:
                return "OK";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method not allowed";
            default:
                return "Internal Server Error";
        }
    }

    /**
     * Определить количество вхождений подстроки subStr в строку origin
     * @param origin подстрока, которая ищется в оригинальной строке
     * @param subStr строка, в которой проходит поиск подстроки
     * @return количество вхождений
     */
    private int subStrInStr(String origin, String subStr) {
        int count = 0;
        while (origin.contains(subStr)){
            origin = origin.replaceFirst(subStr, "");
            count++;
        }
        return count ;
    }

    /**
     * Определить, обращается ли путь к файлам, которые находятся вне данной директории
     * @param url
     * @return
     */
    private Boolean isURLDangerous(String url) {
        int nesting = 0;
        int backnesting = subStrInStr(url, "/..");
        if (backnesting > 0) {
            nesting = subStrInStr(url, "/") - 2 * backnesting;
            return nesting < 0;
        }
        return false;
    }
}

public class Server implements Runnable {

    private int serverPort = 8080;
    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private Thread runningThread = null;
    private ExecutorService threadPool =
            Executors.newFixedThreadPool(5);

    public Server(int port) {
        this.serverPort = port;
    }

    /**
     * Запустить сервер
     */
    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        while(! isStopped()){
            Socket clientSocket = null;
            try {
                // ждём, пока кто нибудь подключится
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
                    new WorkerRunnable(clientSocket));

        }

        System.out.println("Server Stopped.");
    }

    /**
     * @return флаг остновки
     */
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Останавить работу сервера
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


    /**
     * Запустить сервер на указанном порте
     */
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

// 393 -> 380 -> 350 -> 314 -> 352
