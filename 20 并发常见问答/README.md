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





