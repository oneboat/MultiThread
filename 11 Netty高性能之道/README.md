### 1、为什么都说Netty是高性能的RPC框架？
答：

 - 传输：netty使用异步非阻塞通信，使用 epoll 替代了传统的select/poll
- 协议： Netty 采用了串行无锁化设计，在 IO 线程内部进行串行操作，避免多线程竞争导致的性能下降。
- 线程：按照Reactor模式设计和实现，使用NioEventLoop 聚合了多路复用器 Selector，由于读写操作都是非阻塞的，这就可以充分提升 IO 线程的运行效率，避免由于频繁 IO 阻塞导致的线程挂起。
- 零拷贝：接收和发送 ByteBuffer使用堆外直接内存进 Socket读写、y 提供了组合 Buffer 对象，可以聚合多个 ByteBuffer 对象、的文件传输采用了 transferTo()方法，它可以直接将文件缓冲区的数据发送到目标 Channel，避免了传统通过循环 write()方式导致的内存拷贝问题。

### 2、服务端的Socket在哪里开始初始化？

答： 在NioServerSocketChannel类的newSocket方法中，调用provider.openServerSocketChannel()开始初始化。

    private static ServerSocketChannel newSocket(SelectorProvider provider) {
	    try {
	          return provider.openServerSocketChannel();
	    } catch (IOException e) {
		    throw new ChannelException("Failed to open a server socket.", e);
	    }
    }


### 3、服务端的Socket在哪里开始accept连接？

答： 在NioServerSocketChannel类的doReadMessages中，javaChannel()返回NioServerSocketChannel对应的ServerSocketChannel。ServerSocketChannel.accept返回客户端的socketChannel 。把 NioServerSocketChannel 和 socketChannel 封装成 NioSocketChannel，并缓存到readBuf。遍历redBuf中的NioSocketChannel，触发各自pipeline的ChannelRead事件，从pipeline的head开始遍历，最终执行ServerBootstrapAcceptor的channelRead方法。

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = javaChannel().accept();
        try {
            if (ch != null) {
                buf.add(new NioSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            logger.warn("Failed to create a new channel from an accepted socket.", t);
            try {
                ch.close();
            } catch (Throwable t2) {
                logger.warn("Failed to close a socket.", t2);
            }
        }
        return 0;
    }



