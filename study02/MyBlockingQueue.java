package cn.tf.thread.lesson5;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<E> {

    private Queue<E> queue;
    private ReentrantLock lock;
    private Condition putCondition,takeCondition ;
    private  final int CAPACITY  = 12;

    public MyBlockingQueue() {
        this.queue = new ArrayDeque<>(CAPACITY);
        this.lock = new ReentrantLock();
        this.putCondition = lock.newCondition();
        this.takeCondition = lock.newCondition();
    }

    //将生产的消息放入队列
    public void put(E e) {
        try{
            lock.lock();
            while(queue.size() >= CAPACITY){
                System.out.println("队列已满,线程"+Thread.currentThread().getName()
                        + "正在等待空位,需要添加的数据是:"+e);
                putCondition.await();
            }
            queue.add(e);
            System.out.println("已添加的数据是:"+e+"，已有"+queue.size()+"个元素加入队列");
            takeCondition.signalAll();
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    //获取队列中的数据
    public Object take() {
        E element = null;
        try{
            lock.lock();
            while (queue.isEmpty()) {
                System.out.println("当前队列为空,线程"+Thread.currentThread().getName() + "正在等待数据进入");
                takeCondition.await();
            }
            element = queue.poll();
            System.out.println("消费掉的数据是:"+element+",队列中还剩下:"+queue.size()+"个元素");
            putCondition.signalAll();
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }finally {
            lock.unlock();
        }
        return element;
    }
}
