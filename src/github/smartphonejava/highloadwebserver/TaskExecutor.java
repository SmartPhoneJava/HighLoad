package github.smartphonejava.highloadwebserver;

public class TaskExecutor implements Runnable {
    private BlockingQueue<Runnable> taskQueue;
    private Thread newThread;
    boolean isStopped = false;

    TaskExecutor(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
        newThread = new Thread(this);
        newThread.start();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Runnable task;
                synchronized (taskQueue) {
                    task = (Runnable) taskQueue.remove();
                }
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

    }

    public void stop() {
        newThread.interrupt();
    }

}