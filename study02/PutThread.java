package cn.tf.thread.lesson5;

public class   PutThread extends Thread {
    int value=0;
    MyBlockingQueue queue;
    PutThread(int value,MyBlockingQueue queue){
        this.value = value;
        this.queue = queue;
    }

    @Override
    public void run() {
        queue.put(value);
    }
}
