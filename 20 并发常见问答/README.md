## 1、线程创建
### 1.1、有哪些方法创建线程？
- 1.继承Thread类 （真正意义上的线程类），是Runnable接口的实现。

- 2.实现Runnable接口，并重写里面的run方法

- 3 .应用程序可以使用Executor框架来创建线程池。Executor框架是juc里提供的线程池的实现。

- 4.实现Callable接口通过FutureTask包装器来创建Thread线程

### 1.2、如何通过 Java 创建进程？
- 通过 Runtime 类的 exec() 方法来创建进程

		//打开计算器
 		Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("calc");
        process.exitValue();

- 通过 ProcessBuilder 创建进程

	    //打开记事本
	    ProcessBuilder build = new ProcessBuilder("notepad");
	    build.start();


## 2、线程执行
### 2.1 如何通过 Java API 启动线程？

thread.start启动

### 2.2 当有线程 T1、T2 以及 T3，如何实现 T1 -> T2 -> T3 的执行顺序？

- 方法一：在下一个线程start之前先执行前一个线程的join


		public class Question2 {
		    public static void main(String[] args) throws InterruptedException {
		        Thread t1 = new Thread(Question2::action,"t1");
		        Thread t2 = new Thread(Question2::action,"t2");
		        Thread t3 = new Thread(Question2::action,"t3");
		
		        t1.start();
		        t1.join();
		        t2.start();
		        t2.join();
		        t3.start();
		        t3.join();
		    }
		    private static void action(){
		        System.out.println("线程正在执行："+Thread.currentThread().getName());
		    }
		}

- 方法二：通过自旋来验证线程是否执行完毕

		 private static void threadLoop(){
	        Thread t1 = new Thread(Question2::action,"t1");
	        Thread t2 = new Thread(Question2::action,"t2");
	        Thread t3 = new Thread(Question2::action,"t3");
	        t1.start();
	        while(t1.isAlive()){
	        }
	        t2.start();
	        while(t2.isAlive()){
	        }
	        t3.start();
	        while(t3.isAlive()){
	        }
	    }

- 方法三： 通过线程的wait方法


		private static void threadWait(){
	        Thread t1 = new Thread(Question2::action,"t1");
	        Thread t2 = new Thread(Question2::action,"t2");
	        Thread t3 = new Thread(Question2::action,"t3");
	        threadStartAndWait(t1);
	        threadStartAndWait(t2);
	        threadStartAndWait(t3);
	    }
	
	    private static void threadStartAndWait(Thread thread){
	        if(Thread.State.NEW .equals(thread.getState())){
	            thread.start();
	        }
	        while(thread.isAlive()){
	            synchronized (thread){
	                try {
	                    thread.wait();
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

## 3、线程中止
### 3.1 如何停止一个线程？
- 设置退出标志，使线程正常退出，也就是当run()方法完成后线程终止

- 使用interrupt()方法中断线程

- 使用stop方法强行终止线程（不推荐使用，Thread.stop, Thread.suspend, Thread.resume 和Runtime.runFinalizersOnExit 这些终止线程运行的方法已经被废弃，使用它们是不安全的！）


### 3.2 请说明 Thread interrupt()、is Interrupted() 以及 interrupted() 的区别以及意义？

- Thread interrupt()：发出中断请求，设置中断状态，并不会立刻中断当前线程，只有等当前线程阻塞在类似sleep和wait等操作上才会执行
- is Interrupted() :判断中断状态（不清除中断状态）
- interrupted():判断中断状态（清除中断状态）

## 4、线程异常
### 4.1 当线程遇到异常时，到底发生了什么？
java的Thread是一个包装，它由GC做垃圾回收，而JVM的Thread是一个OS Thread，由JVM管理。

当一个未捕获异常将造成线程中断的时候JVM会使用Thread.getUncaughtExceptionHandler()来查询线程的UncaughtExceptionHandler并将线程和异常作为参数传递给handler的uncaughtException()方法进行处理。


### 4.2 当线程遇到异常时，如何捕获？
通过这种捕获的方式，在高并发场景下防止在日志文件打印过多的异常堆栈信息出来。


 	Thread.setDefaultUncaughtExceptionHandler((thread,throwable)->{
            System.out.printf("线程[%s]遇到了异常,详细信息是：%s\n",
                    thread.getName(),throwable.getMessage());
        });

### 4.3 当线程遇到异常时，ThreadPoolExecutor 如何捕获异常？

通过覆盖ThreadPoolExecutor.afterExecute 方法，我们才能捕获到ThreadPoolExecutor任务的异常

 	ThreadPoolExecutor executorService = new ThreadPoolExecutor(
                1,1,0,TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()){
                    @Override
                    protected void afterExecute(Runnable r,Throwable e){
                        System.out.printf("线程[%s]遇到了异常,详细信息是：%s\n",
                                Thread.currentThread().getName(),e.getMessage());
                    }
        };


## 5、线程状态
### 5.1 Java 线程有哪些状态，分别代表什么含义？

- NEW：新建、初始化
- RUNNABLE：可运行状态
- BLOCKED：阻塞中
- WAITING：无限等待中
- TIMED_WAITING：有时间限制的等待中
- TERMINATED：结束、死亡状态


### 5.2 如何获取当前 JVM 所有的线程状态？
通过ThreadMXBean获取。

	 public static void main(String[] args) {
	        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	        long[] threadIds = bean.getAllThreadIds();
	        for(long threadId:threadIds){
	            ThreadInfo threadInfo = bean.getThreadInfo(threadId);
	            System.out.println(threadInfo.toString());
	        }
	    }

### 5.3 如何获取线程的资源消费情况？
使用com.sun.management.ThreadMXBean。


	import com.sun.management.ThreadMXBean;
	import java.lang.management.ManagementFactory;
	import java.lang.management.ThreadInfo;
	
	public class Question5 {

	    public static void main(String[] args) {
	
	        ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
	        long[] threadIds = bean.getAllThreadIds();
	        for(long threadId:threadIds){
	            ThreadInfo threadInfo = bean.getThreadInfo(threadId);
	            System.out.println(threadInfo.toString());
	            long bytes = bean.getThreadAllocatedBytes(threadId);
	            long mbytes = bytes /1024/1024;
	            System.out.printf("线程[ID:%d]分配 %s MB内存\n",threadId,mbytes);
	        }
	    }
	}

## 6、线程同步
### 6.1 请说明synchronized 关键字在修饰方法与代码块中的区别？

- 修饰一个类：其作用的范围是synchronized后面括号括起来的部分，作用的对象是这个类的所有对象；

- 修饰一个方法：被修饰的方法称为同步方法，其作用的范围是整个方法，作用的对象是调用这个方法的对象；

- 修饰一个静态的方法：其作用的范围是整个方法，作用的对象是这个类的所有对象；

- 修饰一个代码块：被修饰的代码块称为同步语句块，其作用范围是大括号{}括起来的代码块，作用的对象是调用这个代码块的对象；

### 6.2 请说明 synchronized关键字与ReentrantLock 之间的区别？

-  ReentrantLock支持对锁的公平性(fairness)控制, synchronized 关键字不支持公平性的控制。 构造函数 ReentrantLock(boolean fair) 可以通过参数 boolean 值指定锁是需要将所提供给等待时间最长的线程还是随机竞争获得锁。
- ReentrantLock 提供了一个跟synchronized 关键字不具备的方法 tryLock() 。 该方法仅仅当锁未被其他线程占用的时， 才会获取锁， 这样可以减少同一时刻阻塞在同一个锁上的线程数量。
- ReentrantLock 在锁定期间， 是可以被其他线程打断的 （interrupt）, synchronized 关键词的线锁修饰的方法是可以被长期或一直阻塞。
- ReentrantLock 还提供了获取所有等待锁线程的List 的方法以及提供了条件变量Condition 的构造方法newCondition()

### 6.3 请解释偏向锁对synchronized与ReentrantLock 的价值？

偏向锁只对synchronized 有用，而 ReentrantLock 已经实现了偏向锁。

## 7、线程通讯
### 7.1 为什么 wait() 和 notify() 以及 notifyAll() 方法属于 Object ，并解释它们的作用？

- Java中，任何对象都可以作为锁，既然wait是放弃对象锁，当然就要把wait定义在这个对象所属的类中。更通用一些，由于所有类都继承于Object，我们完全可以把wait方法定义在Object类中，这样，当我们定义一个新类，并需要以它的一个对象作为锁时，不需要我们再重新定义wait方法的实现，而是直接调用父类的wait(也就是Object的wait)，此处，用到了Java的继承。

- 如果wait定义在Thread类里面，这样做有一个非常大的问题，一个线程完全可以持有很多锁，你一个线程放弃锁的时候，到底要放弃哪个锁？当然了，这种设计并不是不能实现，只是管理起来更加复杂。

wait(): 获得锁的对象，释放锁，当前线程又被阻塞，等同于Java 5 LockSupport 中的park方法

notify(): 已经获得锁，唤起一个被阻塞的线程，等同于Java 5 LockSupport 中的unpark()方法

### 6.2 为什么 Object wait() 和 notify() 以及 notifyAll() 方法必须 synchronized 之中执行？

- synchronized会用锁来让某个对象的方法或者某个类的静态方法在同一时刻只能有一个线程来执行，其他的调用会等待。
- 因为这三个方法都是释放锁的，如果没有synchronize先获取锁就调用会引起异常

## 8、线程退出
### 8.1 当主线程退出时，守候子线程会执行完毕吗？
不一定执行完毕

### 8.2 请说明 ShutdownHook 线程的使用场景，以及如何触发执行？
在jvm中增加一个关闭的钩子，当jvm关闭的时候，会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，当系统执行完这些钩子后，jvm才会关闭。所以这些钩子可以在jvm关闭的时候进行内存清理、对象销毁等操作。

使用场景：

- 程序正常退出
- 使用System.exit()
- 终端使用Ctrl+C触发的中断
- 系统关闭
- 使用Kill pid命令干掉进程
- Spring 中 AbstractApplicationContext 的registerShutdownHook()


		public class Question6 {
		    public static void main(String[] args) {
		        Runtime runtime = Runtime.getRuntime();
		        runtime.addShutdownHook(new Thread(Question6::action, "Shutdown Hook Question"));
		    }
		    private static void action() {
		        System.out.printf("线程[%s] 正在执行...\n", Thread.currentThread().getName());  // 2
		    }
		}

### 8.3 如何确保在主线程退出前，所有线程执行完毕？
通过线程组的方式来处理
	
	public class Question7 {
	
	    public static void main(String[] args) throws InterruptedException {
	        Thread t1 = new Thread(Question7::action,"t1");
	        Thread t2 = new Thread(Question7::action,"t2");
	        Thread t3 = new Thread(Question7::action,"t3");
	        t1.start();
	        t2.start();
	        t3.start();
	        Thread  mainThread = Thread.currentThread();
	        ThreadGroup threadGroup = mainThread.getThreadGroup();
	        int count = threadGroup.activeCount();
	        Thread[] threads = new Thread[count];
	        threadGroup.enumerate(threads,true);
	        for(Thread thread: threads){
	               System.out.printf("当前活跃线程: %s\n", thread.getName());
	        }
	    }
	    private static void action(){
	        System.out.println("线程正在执行："+Thread.currentThread().getName());
	    }
	}

## 9、线程安全集合
### 9.1 请在 Java 集合框架以及 J.U.C 框架中各举出List、Set以及 Map 的 实现？

Java 集合框架: LinkedList、ArrayList、HashSet、TreeSet、HashMap

J.U.C 框架: CopyOnWriteArrayList、CopyOnWriteArraySet、ConcurrentSkipListSet、ConcurrentSkipListMap、ConcurrentHashMap


### 9.2 如何将普通 Lis t、Set 以及 Map 转化为线程安全对象？ 

通过 Collections#sychronized，等于外面包装了一层

	public class Question8 {
	    public static void main(String[] args) {
	        // Java 9 的实现
	        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
	        // Java 9 + of 工厂方法，返回 Immutable 对象
	        list = List.of(1, 2, 3, 4, 5);
	        Set<Integer> set = Set.of(1, 2, 3, 4, 5);
	        Map<Integer, String> map = Map.of(1, "A");
	        // 以上实现都是不变对象，不过第一个除外
	        // 通过 Collections#sychronized* 方法返回
	        // Wrapper 设计模式（所有的方法都被 synchronized 同步或互斥）
	        list = Collections.synchronizedList(list);
	        set = Collections.synchronizedSet(set);
	        map = Collections.synchronizedMap(map);
	        list = new CopyOnWriteArrayList<>(list);
	        set = new CopyOnWriteArraySet<>(set);
	        map = new ConcurrentHashMap<>(map);
	    }
	}


## 10、线程安全List
### 10.1 请说明List、Vector以及CopyOnWriteArrayList 的相同点和不同点？

相同点：

- Vector、CopyOnWriteArrayList 是 List 的实现。

不同点：

- Vector 是同步的,任何时候不加锁。并且在设计中有个 interator ,返回的对象是 fail-fast；

- CopyOnWriteArrayList 读的时候是不加锁；弱一致性，while true的时候不报错。


### 10.2 请说明 Collections#synchromizedList(List) 与 Vector 的相同点和不同点？

相同点：

- 都是synchromized 的实现方式。

不同点：

- synchromized 返回的是list, 实现原理方式是 Wrapper 实现；
-  Vector 是 List 的实现。实现原理方式是非 Wrapper 实现。

## 11、线程安全Set
### 11.1 请至少举出三种线程安全的 Set 实现？

synchronizedSet、CopyOnWriteArraySet、ConcurrentSkipListSet

### 11.2 在 J.U.C 框架中，存在HashSet的线程安全实现？如果不存在的话，要如何实现？

不存在。可以通过实现Set，然后在里面包装ConcurrentHashMap来实现


 	private static class ConcurrentHashSet<E> implements Set<E> {

        private final Object OBJECT = new Object();

        private final ConcurrentHashMap<E, Object> map = new ConcurrentHashMap<>();

        private Set<E> keySet() {
            return map.keySet();
        }

        @Override
        public int size() {
            return keySet().size();
        }

        @Override
        public boolean isEmpty() {
            return keySet().isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return keySet().contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return keySet().iterator();
        }
        
		...
    }

### 11.3 当 Set#iterator() 方法返回 Iterator 对象后，能否在其迭代中，给 Set 对象添加新的元素？

不一定；Set 在传统实现中，会有fail-fast问题；而在J.U.C中会出现弱一致性，对数据的一致性要求较低，是可以给 Set 对象添加新的元素。

## 12、线程安全Map
### 12.1 请说明 Hashtable、HashMap 以及ConcurrentHashMap 的区别？

Hashtable： key、value值都不能为空; 数组结构上，通过数组和链表实现。

HashMap： key、value值都能为空；数组结构上，当阈值到达8时，通过红黑树实现。

ConcurrentHashMap： key、value值都不能为空；JDK 1.6中，采用分离锁的方式，在读的时候，部分锁；写的时候，完全锁。而在JDK 1.7、1.8中，读的时候不需要锁的，写的时候需要锁的。并且JDK 1.8中在为了解决Hash冲突，采用红黑树解决。

### 12.2 请说明 ConcurrentHashMap 与 ConcurrentSkipListMap 各自的优势与不足？

在 java 6 和 8 中，ConcurrentHashMap 写的时候，是加锁的，所以内存占得比较小，而 ConcurrentSkipListMap 写的时候是不加锁的，内存占得相对比较大，通过空间换取时间上的成本，速度较快，但比前者要慢，ConcurrentHashMap 基本上是常量时间。ConcurrentSkipListMap 读和写都是log N实现，高性能相对稳定。

## 13、线程安全 Queue
### 13.1 请说明 BlockingQueue 与 Queue 的区别？
BlockingQueue 继承了 Queue 的实现；put 方法中有个阻塞的操作（InterruptedException），当队列满的时候，put 会被阻塞；当队列空的时候，put方法可用。take 方法中，当数据存在时，才可以返回，否则为空。


### 13.2 请说明 LinkedBlockingQueue 与 ArrayBlockingQueue 的区别？

LinkedBlockingQueue 是链表结构；有两个构造器，一个是（Integer.MAX_VALUE)，无边界，另一个是(int capacity)，有边界；ArrayBlockingQueue 是数组结构；有边界。


## 14、锁 LOCK
### 14.1 请说明 ReentrantLock 与 ReentrantReadWriteLock 的区别？

- ReentrantLock虽然可以灵活地实现线程安全，但是他是一种完全互斥锁，即某一时刻永远只允许一个线程访问共享资源，不管是读数据的线程还是写数据的线程。这导致的结果就是，效率低下。
- ReentrantReadWriteLock中维护了读锁和写锁。允许线程同时读取共享资源；但是如果有一个线程是写数据，那么其他线程就不能去读写该资源。即会出现三种情况：读读共享，写写互斥，读写互斥。


### 14.2 请解释 ReentrantLock 为什么命名为重进入？
一个线程是否可多次获得同一个锁

方法a和方法b被相同可重入锁锁定，a方法里调用了b方法，线程1调用a方法，如果是不可重入锁，会在b方法处阻塞，而可重入锁，由于ab方法持有的锁和调用线程一样，所以可以无阻碍执行。

### 14.3 请说明 Lock#lock() 与 Lock#lockInterruptibly() 的区别？
Lock()提供了无条件地轮询获取锁的方式，lockInterruptibly()提供了可中断的锁获取方式。

- lock： lock获取锁过程中，忽略了中断，在成功获取锁之后，再根据中断标识处理中断，即selfInterrupt中断自己。acquireQueued，在for循环中无条件重试获取锁，直到成功获取锁，同时返回线程中断状态。该方法通过for循正常返回时，必定是成功获取到了锁。
- lockInterruptibly： 可中断加锁，即在锁获取过程中不处理中断状态，而是直接抛出中断异常，由上层调用者处理中断。源码细微差别在于锁获取这部分代码，这个方法与acquireQueue差别在于方法的返回途径有两种，一种是for循环结束，正常获取到锁；另一种是线程被唤醒后检测到中断请求，则立即抛出中断异常，该操作导致方法结束。

## 15、条件变量 CONDITION
### 15.1 请举例说明 Condition 使用场景？
- CoutDownLatch (condition 变种)
- CyclicBarrier (循环屏障)
- 信号量/灯（Semaphore) java 9
- 生产者和消费者
- 阻塞队列

### 15.2 请解释 Condition await() 和 signal() 与 Object wait() 和 notify() 的相同与差异？
- 相同点：都是阻塞和释放
- 不同点： Java Thread 对象和实际 JVM 执行的 OS Thread 不是相同对象，JVM Thread 回调 Java Thread.run() 方法，同时 Thread 提供一些 native 方法来获取 JVM Thread 状态，当JVM thread 执行后，自动 notify()了。

		while (thread.isAlive()) { // Thread 特殊的 Object
	            // 当线程 Thread isAlive() == false 时，thread.wait() 操作会被自动释放
	            synchronized (thread) {
	                try {
	                    thread.wait(); // 到底是谁通知 Thread -> thread.notify();
						// LockSupport.park(); // 死锁发生
	                } catch (Exception e) {
	                    throw new RuntimeException(e);
	                }
	            }


## 16、屏障 BARRIERS
### 16.1 请说明 CountDownLatch 与 CyclicBarrier 的区别？

- CountDownLatch计数器只能使用一次。CyclicBarrier则可以调用其reset()方法进行重置多次使用。
- CountDownLatch使用countDown()+await()进行处理，需要通过countDown的次数到设置的次数，其await()才不会阻塞，往往是一个主线程中使用CountDownLatch然后控制所有子线程。CyclicBarrier的同步屏障是针对对应的子线程的，但同时设置了new CyclicBarrier(N)也需要对应N次的线程(部分主子线程)来执行await()，才能继续执行await()后面的代码。

### 16.2 请说明 Semaphore 的使用场景？

Semaphore，是JDK1.5的java.util.concurrent并发包中提供的一个并发工具类。

Semaphore经常用于限制获取某种资源的线程数量。

### 16.3 请通过 Java 1.4 的语法实现一个 CountDownLatch ？

	import java.util.concurrent.ExecutorService;
	import java.util.concurrent.Executors;
	import java.util.concurrent.locks.Condition;
	import java.util.concurrent.locks.Lock;
	import java.util.concurrent.locks.ReentrantLock;
	
	public class LegacyCountDownLatchDemo {
	
	    public static void main(String[] args) throws InterruptedException {
	
	        // 倒数计数 5
	        MyCountDownLatch latch = new MyCountDownLatch(5);
	
	        ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	        for (int i = 0; i < 5; i++) {
	            executorService.submit(() -> {
	                action();
	                latch.countDown(); // -1
	            });
	        }
	
	        // 等待完成
	        // 当计数 > 0，会被阻塞
	        latch.await();
	
	        System.out.println("Done");
	
	        // 关闭线程池
	        executorService.shutdown();
	    }
	
	    private static void action() {
	        System.out.printf("线程[%s] 正在执行...\n", Thread.currentThread().getName());  // 2
	    }
	
	    /**
	     * Java 1.5+ Lock 实现
	     */
	    private static class MyCountDownLatch {
	
	        private int count;
	        private final Lock lock = new ReentrantLock();
	        private final Condition condition = lock.newCondition();
	
	        private MyCountDownLatch(int count) {
	            this.count = count;
	        }
	        public void await() throws InterruptedException {
	            // 当 count > 0 等待
	            if (Thread.interrupted()) {
	                throw new InterruptedException();
	            }
	
	            lock.lock();
	            try {
	                while (count > 0) {
	                    condition.await(); // 阻塞当前线程
	                }
	            } finally {
	                lock.unlock();
	            }
	        }
	        public void countDown() {
	
	            lock.lock();
	            try {
	                if (count < 1) {
	                    return;
	                }
	                count--;
	                if (count < 1) { // 当数量减少至0时，唤起被阻塞的线程
	                    condition.signalAll();
	                }
	            } finally {
	                lock.unlock();
	            }
	        }
	    }
	
	    /**
	     * Java < 1.5 实现
	     */
	    private static class LegacyCountDownLatch {
	        private int count;
	        private LegacyCountDownLatch(int count) {
	            this.count = count;
	        }
	
	        public void await() throws InterruptedException {
	            // 当 count > 0 等待
	            if (Thread.interrupted()) {
	                throw new InterruptedException();
	            }

	            synchronized (this) {
	                while (count > 0) {
	                    wait(); // 阻塞当前线程
	                }
	            }
	        }
	        public void countDown() {
	            synchronized (this) {
	                if (count < 1) {
	                    return;
	                }
	                count--;
	                if (count < 1) { // 当数量减少至0时，唤起被阻塞的线程
	                    notifyAll();
	                }
	            }
	        }
	    }
	}

## 17、线程池 THREAD POOL
### 17.1 请问 J.U.C 中内建了几种 ExecutorService 实现？

1.5：ThreadPoolExecutor、ScheduledThreadPoolExecutor

1.7：ForkJoinPool

### 17.2 请分别解释 ThreadPoolExecutor 构造器参数在运行时的作用？

- corePoolSize：核心线程数量，当有新任务在execute()方法提交时，会执行以下判断：

	- 如果运行的线程少于 corePoolSize，则创建新线程来处理任务，即使线程池中的其他线程是空闲的；
	- 如果线程池中的线程数量大于等于 corePoolSize 且小于 maximumPoolSize，则只有当workQueue满时才创建新的线程去处理任务；
	- 如果设置的corePoolSize 和 maximumPoolSize相同，则创建的线程池的大小是固定的，这时如果有新任务提交，若workQueue未满，则将请求放入workQueue中，等待有空闲的线程去从workQueue中取任务并处理；
	- 如果运行的线程数量大于等于maximumPoolSize，这时如果workQueue已经满了，则通过handler所指定的策略来处理任务；

- maximumPoolSize：最大线程数量；
- workQueue：等待队列，当任务提交时，如果线程池中的线程数量大于等于corePoolSize的时候，把该任务封装成一个Worker对象放入等待队列；
- keepAliveTime: 超时时间。
- threadFactory：它是ThreadFactory类型的变量，用来创建新线程。默认使用Executors.defaultThreadFactory() 来创建线程。使用默认的ThreadFactory来创建线程时，会使新创建的线程具有相同的NORM_PRIORITY优先级并且是非守护线程，同时也设置了线程的名称。
- handler：它是RejectedExecutionHandler类型的变量，表示线程池的饱和策略。如果阻塞队列满了并且没有空闲的线程，这时如果继续提交任务，就需要采取一种策略处理该任务。线程池提供了4种策略：
	- AbortPolicy：直接抛出异常，这是默认策略；
	- CallerRunsPolicy：用调用者所在的线程来执行任务；
	- DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
	- DiscardPolicy：直接丢弃任务；
	-  当然也可以根据应用场景实现 RejectedExecutionHandler 接口，自定义饱和策略，如记录日志或持久化存储不能处理的任务

### 17.3 如何获取 ThreadPoolExecutor 正在运行的线程？

覆写ThreadPoolExecutor的beforeExecute和afterExecute方法可以做到，但是不是很好，因为这两个方法被限定在ExecutorPool里面，因为有的框架(例如netty)并没有继承ThreadPoolExector这个类，而是继承了AbstractExecutorService，此时就没有before和after的方法。

下面来看另一种方法：通过ThreadFactory来实现

	public class ThreadPoolExecutorThreadQuestion {
	
	    public static void main(String[] args) throws InterruptedException {
	        ExecutorService executorService = Executors.newCachedThreadPool();
	
	        Set<Thread> threadsContainer = new HashSet<>();
	        setThreadFactory(executorService, threadsContainer);
	        for(int i=0;i<9;i++){
	            executorService.submit(()->{
	
	            });
	        }
	        // 线程池等待执行 3 ms
	        executorService.awaitTermination(3, TimeUnit.MILLISECONDS);
	        threadsContainer.stream()
	                .filter(Thread::isAlive)
	                .forEach(thread -> {
	                    System.out.println("线程池创造的线程 : " + thread);
	                });
	        Thread mainThread = Thread.currentThread();
	        ThreadGroup mainThreadGroup = mainThread.getThreadGroup();
	        int count = mainThreadGroup.activeCount();
	        Thread[] threads = new Thread[count];
	        mainThreadGroup.enumerate(threads, true);
	
	        Stream.of(threads)
	                .filter(Thread::isAlive)
	                .forEach(thread -> {
	                    System.out.println("线程 : " + thread);
	                });
	        // 关闭线程池
	        executorService.shutdown();
	
	    }
	
	    private static void setThreadFactory(ExecutorService executorService, Set<Thread> threadsContainer) {
	        if (executorService instanceof ThreadPoolExecutor) {
	            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
	            ThreadFactory oldThreadFactory = threadPoolExecutor.getThreadFactory();
	            threadPoolExecutor.setThreadFactory(new DelegatingCountFactory(oldThreadFactory, threadsContainer));
	        }
	    }
	    private static class DelegatingCountFactory implements ThreadFactory {
	        private final ThreadFactory delegate;
	        private final Set<Thread> threadsContainer;
	        private DelegatingCountFactory(ThreadFactory delegate, Set<Thread> threadsContainer) {
	            this.delegate = delegate;
	            this.threadsContainer = threadsContainer;
	        }
	        @Override
	        public Thread newThread(Runnable r) {
	            Thread thread = delegate.newThread(r);
	            // cache thread
	            threadsContainer.add(thread);
	            return thread;
	        }
	    }
	}



## 18、FUTURE
### 18.1 如何获取 Future 对象？

一旦我们拥有了一个ExecutorService对象，我们只需要调用submit()方法，把Callable作为参数传递给它。submit()方法会负责该任务的启动以及返回一个FutureTask对象。这个FutureTask对象是Future接口的一个实现。

### 18.2 请举例 Future get() 以及 get(long,TimeUnit) 方法的使用场景？
不带参数的get方法是阻塞方法，只要线程为返回结果就会一直阻塞直到有结果为止。

get(long,TimeUnit) 是超时等待，若指定时间内还没有得到线程返回值，会抛出TimeoutException的异常



### 18.3 如何利用 Future 优雅地取消一个任务的执行？
可以使用Future.cancel(boolean) 方法去告诉该executor停止操作并且中断它潜在的线程：

Future future = newSquareCalculator().calculate(4);
booleancanceled = future.cancel(true);

上述例子中的Future实例，不会结束他的运算操作。事实上，如果我们试图在调用cancel()方法之后，调用该实例的get()方法的话，将会产生一个CancelllationException异常。Future.isCancelled()将会告诉我们，是否Future已经被取消了。这对于避免CancellationException异常很有帮助。

在我们调用cancel()方法时，是有可能失败的。在这种情况下，它的返回值将是false.请注意：cancel()方法会接收一个Boolean值作为参数-它会控制正在执行该task的线程是否应该被中断。

## 19、VOLATILE 变量
### 19.1 在 Java 中，volatile 保证的是可见性还是原子性？

volatile 既有可见性又有原子性，可见性是一定的，原子性是看情况的。对象类型和原生类型都是可见性，原生类型是原子性。原生类型例如int 、double等。用volatile修饰long和double可以保证其操作原子性。

### 19.2 在 Java 中，volatile long 和 double 是线程安全的吗？ 
是线程安全的


### 193 在 Java 中，volatile 的底层实现是基于什么机制？

volatile的底层是通过lock前缀指令、内存屏障来实现的。

lock前缀指令其实就相当于一个内存屏障。内存屏障是一组CPU处理指令，用来实现对内存操作的顺序限制。volatile的底层就是通过内存屏障来实现的。

编译器和执行器 可以在保证输出结果一样的情况下对指令重排序，使性能得到优化。插入一个内存屏障，相当于告诉CPU和编译器先于这个命令的必须先执行，后于这个命令的必须后执行。


## 20、原子操作 ATOMIC
### 20.1 为什么 AtomicBoolean 内部变量使用 int 实现，而非 boolean？
操作系统有 X86 和 X64,底层是用C语言实现的，在C语言中是没有boolean类型的，用的是0或1来表示boolean类型，0表示false，1表示true，也就是说0表示假，非0表示真。与其说在调用底层的时候将Java中的value（true和false）转换为0或1还不如在一开始记录value的时候，就将value用int型来记录，这样就省去转换的步骤，而且避免了不必要的错误。


### 20.2 在变量原子操作时，Atomic* CAS 操作比 synchronized 关键字那个更重？

synchronized更重。

单线程的时候，synchronized 更快；而多线程的时候则要分情况讨论。cas在竞争激烈的时候速度反而下降。不难想象反复的失败重试。

CAS 操作也是相对重的操作，它也是实现 synchronized 瘦锁(thin lock)的关键，偏向锁就是避免 CAS（Compare And Set/Swap)操作。


### 20.3 Atomic* CAS 的底层是如何实现的？
- JAVA中的CAS操作都是通过sun包下Unsafe类实现，而Unsafe类中的方法都是native方法，由JVM本地实现。Unsafe中对CAS的实现是C++写的，最后调用的是Atomic:comxchg这个方法，这个方法的实现放在hotspot下的os_cpu包中，说明这个方法的实现和操作系统、CPU都有关系。

- Linux的X86下主要是通过cmpxchgl这个指令在CPU级完成CAS操作的，但在多处理器情况下必须使用lock指令加锁来完成。
