package github.smartphonejava.highloadwebserver;

import java.util.LinkedList;
import java.util.List;

class BlockingQueue<T> {
    private List<Object> queue = new LinkedList<>();
    private int limit;

    BlockingQueue(int limit) {
        this.limit = limit;
    }

    public synchronized void add(Object item) {
        while (this.queue.size() == limit) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (this.queue.size() == 0) {
            notifyAll();
        }
        this.queue.add(item);
    }

    public synchronized Object remove() {
        while (this.queue.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (queue.size() == limit) {
            notifyAll();
        }
        return queue.remove(0);
    }
}