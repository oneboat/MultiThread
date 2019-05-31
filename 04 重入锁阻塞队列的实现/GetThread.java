package cn.tf.thread.lesson5;

public class GetThread    extends Thread {
    MyBlockingQueue queue;
    GetThread(MyBlockingQueue queue){
        this.queue = queue;
    }
    @Override
    public void run() {
        queue.take();
    }
}

