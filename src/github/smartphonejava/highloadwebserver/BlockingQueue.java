package github.smartphonejava.highloadwebserver;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

class Counter {
    private int _c;
    Counter() {
        _c = 0;
    }
    protected void inc() {
        _c++;
    }
    protected void dec() {
        _c--;
    }

    protected int c() {
        return _c;
    }
}

class BlockingQueue {
    private List<WorkerRunnable> queue = new LinkedList<>();
    private int limit;

    Counter wantRemove = new Counter();
    Counter wantAdd = new Counter();

    BlockingQueue(int limit) {
        this.limit = limit;
    }

    public synchronized void _add(WorkerRunnable item) {
        this.queue.add(item);
    }
    public synchronized WorkerRunnable _remove(int i) {
        return this.queue.remove(i);
    }
    public synchronized int size() {
        return queue.size();
    }
    public synchronized void add(WorkerRunnable item) {
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
        //System.out.println("add:"+ this.queue.size());
        this.queue.add(item);
    }

    public synchronized WorkerRunnable remove() {
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
        //System.out.println("remove:"+ this.queue.size());
        return queue.remove(0);
    }

    public void addX(WorkerRunnable item) {
        synchronized (wantAdd) {
            //System.out.println("{ wantAdd");
            if (size() == limit) {
                try {
                    wantAdd.inc();
                    wantAdd.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            _add(item);
            //System.out.println("wantAdd }");
        }
        synchronized (wantRemove) {
            if (wantRemove.c() != 0) {
                wantRemove.dec();
                wantRemove.notify();
            }
        }

        //System.out.println("add:"+ size());

    }

    public WorkerRunnable removeX() {
        WorkerRunnable ra = null;
        synchronized (wantRemove) {
            //System.out.println("{ remove:"+size());
            if (size() == 0) {
                //System.out.println("r strange");
                try {
                    wantRemove.inc();
                    wantRemove.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            //System.out.println("r delete");
            if (size() != 0) {
                ra = _remove(0);
            }
            //System.out.println("remove }");
        }
        synchronized (wantAdd) {
            if (wantAdd.c() != 0) {
                wantAdd.dec();
                wantAdd.notify();
            }
        }
        //System.out.println("remove:"+ size());
        return ra;
    }
}