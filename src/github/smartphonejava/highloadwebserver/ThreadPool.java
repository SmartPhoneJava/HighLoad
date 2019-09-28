package github.smartphonejava.highloadwebserver;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    private BlockingQueue<Runnable> tasks;
    private ArrayList<TaskExecutor> threadsList;
    boolean isStopped = false;

    ThreadPool(int threadsMaxAmount, int tasksMaxAmount) {
        tasks = new BlockingQueue<>(tasksMaxAmount);
        TaskExecutor executor = new TaskExecutor(tasks);
        threadsList = new ArrayList<>();
        for (int i = 0; i < threadsMaxAmount; i++)
            threadsList.add(executor);
    }

    public synchronized void submitTask(Runnable newTask) {
        if (isStopped)
            return;
        tasks.add(newTask);
    }

    public synchronized void stop() {
        isStopped = true;
        for (TaskExecutor thread : threadsList)
            thread.stop();
    }
}
