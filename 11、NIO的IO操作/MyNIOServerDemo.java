package cn.tf.netty.io.myNio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO操作,服务端
 */
public class MyNIOServerDemo {

    private int port = 8080;
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public MyNIOServerDemo(int port) {
        this.port = port;
        try{
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(this.port));
            //采用阻塞式,为了兼容BIO，NIO
            server.configureBlocking(false);
            selector =Selector.open();
            //注册并设置key阻塞
            server.register(selector, SelectionKey.OP_ACCEPT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void listen(){
        System.out.println("启动成功，监听端口是:"+this.port);
        try{
            while(true){
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    process(key);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // //针对于每一种状态给一个反应
    private void process(SelectionKey key) throws IOException {
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);
            key = channel.register(selector,SelectionKey.OP_READ);
        }else if(key.isReadable()) {
            //key.channel 从多路复用器中拿到客户端的引用
            SocketChannel channel = (SocketChannel) key.channel();
            int len = channel.read(buffer);
            if (len > 0) {
                buffer.flip();
                String content = new String(buffer.array(), 0, len);
                key = channel.register(selector, SelectionKey.OP_WRITE);
                key.attach(content);  //在key上携带一个附件，一会再写出去
                System.out.println("读取到的内容是:" + content);
            }
        }else if(key.isWritable()){
                SocketChannel channel  = (SocketChannel) key.channel();
                String context = (String)key.attachment();
                channel.write(ByteBuffer.wrap(("输出："+context).getBytes()));
                channel.close();
        }
    }


    public static void main(String[] args) {
        new MyNIOServerDemo(8080).listen();
    }

}
