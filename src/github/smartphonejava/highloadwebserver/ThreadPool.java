package github.smartphonejava.highloadwebserver;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    private BlockingQueue<Runnable> tasks;
    private List<TaskExecutor> threadsList = new ArrayList();
    boolean isStopped = false;

    ThreadPool(int threadsMaxAmount, int tasksMaxAmount) {
        tasks = new BlockingQueue(tasksMaxAmount);
        TaskExecutor executor = new TaskExecutor(tasks);
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
