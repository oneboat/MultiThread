package cn.tf.thread.lesson5;

import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class MyBlockQueueTest {

    public static void main(String[] args) {
        MyBlockingQueue queue = new MyBlockingQueue();
        for (int i = 0; i < 100; i++) {
            new Thread(new PutThread(i,queue), "PutThread-" + i).start();
        }
        for (int i = 0; i < 100; i++) {
            new Thread(new GetThread(queue), "GetThread-" + i).start();
        }
    }


}
