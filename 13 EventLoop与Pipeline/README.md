## 1、了解Netty服务端的线程池分配规则，线程何时启动。
答：

- 分配规则：EventLoopGroup内部维护一个类为EventExecutor children数组，其大小是nThreads，这样就初始化了一个线程池。如果指定线程池大小，则nThreads是指定值，否则是CPU核数*2；
- 启动时间：NioEventLoop 本身就是一个 SingleThreadEventExecutor，因此 NioEventLoop 的启动，其 实就是 NioEventLoop 所绑定的本地 Java 线程的启动。当 EventLoop 的 execute()第一次被调用时，就会触发 startThread()方法的调用，进而导致 EventLoop 所对应的 Java 本地线程启动。

## 2、了解Netty是如何解决JDK空轮训Bug的？
答： 

- netty通过创建一个新的 Selector，将可用事件重新注册到新的 Selector 中来终止空轮训。
- Selector 每一次轮询都计数 selectCnt++，开始轮询会计时赋值给 timeoutMillis，轮询完成会 计时赋值给 time，这两个时间差会有一个时间差，而这个时间差就是每次轮询所消耗的时间。如果每次轮询消耗的时间为 0，且重复次数超过 512 次，则调用 rebuildSelector()方法，即重构 Selector。
- 重构Selector主要就是先创建一个新的selector，然后将原来Selector 中注册的事件全部取消，最后将可用事件重新注册到新的 Selector 中，并激活


## 3、Netty是如何实现异步串行无锁化编程的？
答：

- 在外部线程调用EventLoop或者channel的一些方法的时候，都会调用InEventLoop()方法检查当前线程是否是NioEventloop中的线程，如果是外部线程，就会将外部线程的的所有操作封装成为一个task，放进EventLoop的MPSCQ里面，然后在NioEventLoop执行过程的第三个部分，这些task会被依次执行样式
- 在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应，在 DefaultChannelPipeline 的构造方法中，将传入的 channel 赋值给字段 this.channel，接着又实例化了两个特殊的字段：tail 与 head，这两个字段是一个双向链表的头和尾。其实在 DefaultChannelPipeline 中，维护了一个 以 AbstractChannelHandlerContext 为节点的双向链表，这个链表是 Netty 实现 Pipeline 机制的关键。






