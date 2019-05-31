## 结合ReentrantLock、Condition实现一个简单的阻塞队列，阻塞队列提供两个方法，一个是put、一个是take

 - 当队列为空时，请求take会被阻塞，直到队列不为空
 - 当队列满了以后，存储元素的线程需要备阻塞直到队列可以添加数据

阻塞队列的代码在MyBlockingQueue.java中，测试代码在MyBlockQueueTest.java中，运行结果在result.txt中。

 
笔记：
Condition 是一个多线程协调通信的工具类，可以让某些线程一起等待某个条件（condition），只有满足条件时，线程才会被唤醒。
当调用 await 方法后，当前线程会释放锁并
等待，而其他线程调用 condition 对象的 signal 或者 signalall 方法通知并被阻塞的线程，然后自己执行 unlock 释放锁，被唤醒的线程获得之前的锁继续执行，最后释放锁。




