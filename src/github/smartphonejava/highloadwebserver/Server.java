package github.smartphonejava.highloadwebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private int serverPort;
    private String path = "";
    private ServerSocket serverSocket = null;
    private boolean isStopped = false;
    private ThreadPool threadPool;

    public Server(int port, int cpu_limit, int thread_limit, String path) {
        this.serverPort = port;
        this.threadPool =  new ThreadPool(thread_limit, thread_limit*10);
        this.path = path;
    }

    /**
     * Запустить сервер
     */
    @Override
    public void run() {
        openServerSocket();

        while(!isStopped()){
            Socket clientSocket = null;
            try {
                // ждём, пока кто нибудь подключится
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            this.threadPool.submitTask(new WorkerRunnable(clientSocket, path));
        }
        System.out.println("Server Stopped.");
    }

    public void shutdown() {
        this.stop();
        System.err.println("shutdown");
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
            this.threadPool.stop();
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
            //e.printStackTrace();
            //System.out.println("Port " + this.serverPort + " is blocked.");
            System.exit(-1);
        }
    }
}

// 393 -> 380 -> 350 -> 314 -> 352
