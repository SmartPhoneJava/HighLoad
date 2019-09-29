package github.smartphonejava.highloadwebserver;

import java.util.concurrent.RecursiveAction;

public class TaskExecutor implements Runnable {
    private BlockingQueue bq;
    boolean isStopped = false;

    TaskExecutor(BlockingQueue bq) {
        this.bq = bq;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                /*
                Runnable task;
                Object obj;
                java.util.concurrent.RecursiveAction etask;
                synchronized (taskQueue) {
                    etask =  (java.util.concurrent.RecursiveAction) taskQueue.remove();
                    //obj = taskQueue.remove();
                }*/

                //task = (Runnable) obj;
                Runnable task = bq.remove();
                task.run();

               // etask =  (java.util.concurrent.RecursiveAction) obj;
/*
                WorkerRunnable etask = bq.remove();
                etask.fork();
                etask.join();*/
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

    }

}