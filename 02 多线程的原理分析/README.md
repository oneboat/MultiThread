### 请列出Happens-before的几种规则

- 程序顺序规则: 一个线程中的每个操作，优先于于该线程中的任意后续操作。
- volatile规则:对一个volatile域的写，优先于任意后续对这个volatile域的读。
- 传递性规则： 如果：a>b,b>c,则 a>c
- start规则：如果线程A执行操作ThreadB.start()（启动线程B），那么A线程的ThreadB.start()操作happens-before于线程B中的任意操作
- join规则:如果线程A执行操作ThreadB.join()并成功返回，那么线程B中的任意操作happens-before于线程A从ThreadB.join()操作成功返回。
- 监视器锁规则:线程A对某个对象加锁，优先于B线程访问这个锁的对象。


### volatile 能使得一个非原子操作变成原子操作吗？为什么？

- Volatile 不保证原子性，例如 volatile int a=0; a 具有可见性，但是 a仍然不具有原子性；原子操作是指：a=0；而非原子操作是指 a，即a=a+1；在java 中 保证原子性的方法是 sync ，lock，unlock

### 哪些场景适合使用Volatile

- 状态标志：用于指示发生了一个重要的一次性事件，例如完成初始化或请求停机
定期 “发布” 观察结果供程序内部使用：例如收集程序的统计信息。
开销较低的“读－写锁”策略：如果读操作远远超过写操作，可以结合使用内部锁和 volatile 变量来减少公共代码路径的开销。

### 如果对一个数组修饰volatile，是否能够保证数组元素的修改对其他线程的可见？为什么？
- 不能，volatile修饰数组时，只是保证其引用地址的可见性。例如
ThreadA在读取ints[0]时，首先要读取ints引用，这个引用是volatile修饰的,在读取这个ints引用时，所有变量都会从主存读取，其中就包含ints[0]