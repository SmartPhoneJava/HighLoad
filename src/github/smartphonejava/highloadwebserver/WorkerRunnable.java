package github.smartphonejava.highloadwebserver;


import sun.nio.ch.FileChannelImpl;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Stream;

import static java.nio.file.Files.newByteChannel;

class Request {
    String method, url;
}


class WorkerRunnable extends RecursiveAction implements Runnable {

    protected Socket clientSocket = null;
    private static String DEFAULT_FILES_DIR;
    private static final int BUFFER_SIZE = 1024;
    private final static char CR = (char) 0x0D;
    private final static char LF = (char) 0x0A;
    private final static String CRLF = "" + CR + LF;
    private int id;

    WorkerRunnable(Socket clientSocket, String path, int id) {
        this.clientSocket = clientSocket;
        this.id = id;
        DEFAULT_FILES_DIR = System.getProperty("user.dir") + path;
    }

    @Override
    public void compute() {
        InputStream input = null;
        OutputStream output = null;
        System.out.println("message from"+ id);

        try {
            clientSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            //e.printStackTrace();
        }
        try {
            System.out.println("message from"+ id);
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();

            String readRequest = readRequest(input);
            Request request = getRequest(readRequest);

            //System.out.println("request:" + request.method + " " + request.url);
            System.out.println("message from"+ id);
            if (request.method == null)
                throw new IOException();
            if (request.method.equals("GET")) {
                sendFile(request.url, output, false);
            } else if (request.method.equals("HEAD")) {
                sendFile(request.url, output, true);
            } else {
                System.out.println("wrong");
                writeResponseHeader(output, 405, null, 0);
            }
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * Получить заголовок запроса
     *
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
     *
     * @param header заголовок запроса
     * @return null если путь до фалйа небезопасен, иначе строка с путем до файла
     */
    private static Request getRequest(String header) {
        Request request = new Request();
        request.url = DEFAULT_FILES_DIR + "/index.html";

        int from = header.indexOf(" ") + 1;
        if (from == 0)
            return request;
        request.method = header.substring(0, from - 1);

        int to = header.indexOf(" ", from);
        if (to == -1)
            return request;

        request.url = java.net.URLDecoder.decode(header.substring(from, to), StandardCharsets.UTF_8);
        if (request.url.lastIndexOf("/") == request.url.length() - 1)
            request.url += "index.html";

        int paramIndex = request.url.indexOf("?");
        if (paramIndex != -1)
            request.url = request.url.substring(0, paramIndex);

        request.url = DEFAULT_FILES_DIR + request.url;

        return request;
    }

    // with non-blocking read
    private void nwr(String url, OutputStream out, Boolean isHead) throws IOException {
        // delete .. and .
        Path resource = Paths.get(url).normalize();

        //int random_number1 = a + (int) (Math.random() * b);

        // now can check if path dangerous
        int index = resource.toString().lastIndexOf(DEFAULT_FILES_DIR);
        if (index < 0)
            throw new IOException("dangerous url:" + resource);
        File file = resource.toFile();

        if (!file.exists())
            throw new IOException("no file");
        System.out.println("message from"+ id);
        String mime = getContentType(file);
        /*if (mime.equals("text/html")) {
            return;
        }*/
        int size = (int) file.length();

        writeResponseHeader(out, 200, mime, size);

        //byte[] bb = ("что то здесь не\n так\n".getBytes(StandardCharsets.UTF_8));
        //System.out.println("lens:"+ bb.length+" "+"что то здесь не\n так\n".length());

       /*
        int i = 0;
        try(Stream<String> lines1 = Files.lines(resource)){
            for( String line : (Iterable<String>) lines1::iterator ) {
                //byte[] bytes = null;
                //Arrays.toString(bytes);
                //System.out.println(line);
                //out.write(line.toCharArray() getBytes(StandardCharsets.UTF_8));
                //out.write("1".getBytes());
            }
        }*/

        if (!isHead) {
            RandomAccessFile aFile = new RandomAccessFile
                    (resource.toString(), "r");
            FileChannel inChannel = aFile.getChannel();

            byte[] byteArray = new byte[3080];
            ByteBuffer buffer = ByteBuffer.wrap(byteArray);
            while(inChannel.read(buffer) > 0)
            {
                buffer.flip();
                out.write(byteArray);
                buffer.clear(); // do something with the data and clear/compact it.
            }
            inChannel.close();
            aFile.close();

            //byte[] byteArray = new byte[18680 ];
            /*
            ByteBuffer buf = ByteBuffer.allocate(2048);;
            FileInputStream in = new FileInputStream(file);
            int len = 1;
            FileChannel fc = in.getChannel();
            int i = 1;
            while (len >= 0) {
                len = fc.read(buf);
                System.out.println("sssss "+ len);
                //out.write(byteArray);
                out.write(buf);
                if (i == 0)
                    break;
                if (len == 0) {
                    i = 0;
                }
            }
            buf.clear();
            in.close();
            fc.close();*/
        }

        //byte[] lines = Files.readAllBytes(resource);
        System.out.println("message from"+ id);
        //if (!isHead)
        //    out.write(lines);
        System.out.println("message from"+ id);
    }

    /**
     * Отправить ответ с телом
     *
     * @param url    путь до файла
     * @param out    поток вывода
     * @param isHead флаг является ли метод запроса HEAD
     */
    private void sendFile(String url, OutputStream out, Boolean isHead) {
        if (url == null) {
            writeResponseHeader(out, 403, null, 0);
            return;
        }
        System.out.println("message from"+ id);
        int code = 200;
        try {
            nwr(url, out, isHead);
        } catch (Exception e) {
            code = url.contains("/index.html") ? 403 : 404;
        }
        if (code != 200)
            writeResponseHeader(out, code, null, 0);
    }

    /**
     * Записать результаты выполнения запроса в заголовки
     *
     * @param out  поток вывода
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
     *
     * @param code          код шибкии
     * @param contentType   тип файла
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
     *
     * @param file файл
     * @return тип файла
     * @throws IOException
     */
    private String getContentType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }

    /**
     * Получить комменатрией к коду ошибки
     *
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

    @Override
    public void run() {
        compute();
    }
}
