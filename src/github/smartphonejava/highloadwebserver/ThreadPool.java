package github.smartphonejava.highloadwebserver;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    //private BlockingQueue<Runnable> tasks;
    private BlockingQueue etasks;
    private ArrayList<Thread> threadsList;
    boolean isStopped = false;

    ThreadPool(int threadsMaxAmount, int tasksMaxAmount) {
        //tasks = new BlockingQueue<>(tasksMaxAmount);
        etasks = new BlockingQueue(tasksMaxAmount);
        threadsList = new ArrayList<>();
        TaskExecutor executor = null;
        System.out.println("create threadsMaxAmount:"+threadsMaxAmount);
        executor = new TaskExecutor(this.etasks);

        for (int i = 0; i < threadsMaxAmount; i++) {
            System.out.println("create i:"+i);
            Thread newThread = new Thread(executor);
            newThread.start();
            threadsList.add(newThread);
        }
    }

    public synchronized boolean isStopped() {
        return isStopped;
    }

    public void submitTask(WorkerRunnable newTask) {
        if (isStopped())
            return;
        //tasks.add(newTask);
        synchronized (etasks) {
            etasks.add(newTask);
        }
    }

    public synchronized void stop() {
        isStopped = true;
        for (Thread thread : threadsList)
            thread.stop();
    }
}
