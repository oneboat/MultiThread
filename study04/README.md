#### 为什么要使用线程池？
合理的使用线程池，可以带来一些好处
1. 降低创建线程和销毁线程的性能开销
2. 提高响应速度，当有新任务需要执行是不需要等待线程创建就可以立马执行
3. 合理的设置线程池大小可以避免因为线程数超过硬件资源瓶颈带来的问题


#### Executors提供的四种线程池:newSingleThreadExecutor,newFixedThreadPool,newCachedThreadPool和newScheduledThreadPool，请说出他们的区别以及应用场景

- newSingleThreadExecutor: 创建一个线程的线程池，若空闲则执行，若没有空闲线程则暂缓。保证所有任务按照指定顺序执行。
- FixedThreadPool：该方法返回一个固定数量的线程池，线程数不变，当有一个任务提交时，若线程池中空闲，则立即执行，若没有，则会被暂缓在一个任务队列中，等待有空闲的线程去执行。用于负载比较大的服务器，为了资源的合理利用，需要限制当前线程数量。
- newCachedThreadPool：返回一个可根据实际情况调整线程个数的线程池，不限制最大线程数量，若用空闲的线程则执行任务，若无任务则不创建线程。并且每一个空闲线程会在 60 秒后自动回收。
- newScheduledThreadPool: 创建一个可以指定线程的数量的线程池，但是这个线程池还带有延迟和周期性执行任务的功能，类似定时器。用于定时及周期性任务执行


#### 线程池有哪几种工作队列？

1. ArrayBlockingQueue：基于数组的先进先出队列，此队列创建时必须指定大小；
2. LinkedBlockingQueue：基于链表的先进先出队列，如果创建时没有指定此队列大小，则默认为 Integer.MAX_VALUE。
3. SynchronousQueue：t同步队列，这个队列比较特殊，它不会保存提交的任务，而是将直接新建一个线程来执行新来的任务。


#### 线程池默认的拒绝策略有哪些
- 1、AbortPolicy：直接抛出异常，默认策略；
- 2、CallerRunsPolicy：用调用者所在的线程来执行任务；
- 3、DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
- 4、DiscardPolicy：直接丢弃任务；
- 当然也可以根据应用场景实现 RejectedExecutionHandler 接口，自定义饱和策略，如记录日志或持久化存储不能处理的任务


#### 如何理解有界队列和无界队列

- 有界队列：就是有固定大小的队列。有界队列满了之后，如果poolSize < maximumPoolsize时，会尝试new 一个Thread的进行救急处理，立马执行对应的runnable任务。
- 无界队列：指的是没有设置固定大小的队列。这些队列的特点是可以直接入列，直到溢出。当然现实几乎不会有到这么大的容量（超过Integer.MAX_VALUE），所以从使用者的体验上，就相当于 “无界”。比如没有设定固定大小的 LinkedBlockingQueue。无界队列适合任务为CPU密集型，且任务生产速度与消费速度相当的场景。
- 一般情况下要配置一下队列大小，设置成有界队列，否则可能会导致OOM发生！
#### 线程池是如何实现线程回收的？ 以及核心线程能不能被回收？
- getTask如果返回null, worker线程会被回收
- 工作线程回收需要满足三个条件：
   	参数allowCoreThreadTimeOut为true，
	该线程在keepAliveTime时间内获取不到任务，即空闲这么长时间，
	当前线程池大小 > 核心线程池大小corePoolSize。

- 核心线程可以被回收，只要设置了allowCoreThreadTimeOut=true

#### FutureTask是什么
- Future 表示一个任务的生命周期，并提供了相应的方法来判断是否已经完成或取消，以及获取任务的结果和取消任务等。
- FutureTask 是 Runnable 和 Future 的结合，如果我们把Runnable 比作是生产者，Future 比作是消费者，那么 FutureTask 是被这两者共享的，生产者运行 run 方法计算结果，当消费者需要时，再调用FutureTask.get()获取结果。

#### Thread.sleep(0)的作用是什么

触发操作系统立刻重新进行一次CPU竞争，竞争的结果也许是当前线程仍然获得CPU控制权，也许会换成别的线程获得CPU控制权。这样就给了其他线程获得CPU控制权的权力，这样就不会假死在那里。

#### 如果提交任务时，线程池队列已满，这时会发生什么

如果核心线程数满了，队列也满了，那么这个时候创建新的线程也就是非核心线程，如果非核心线程数也达到了最大线程数大小，则直接拒绝任务

#### 如果一个线程池中还有任务没有执行完成，这个时候是否允许被外部中断？
允许，可以通过调用shutdown 和shutdownnow两个方法来中断线程的执行。