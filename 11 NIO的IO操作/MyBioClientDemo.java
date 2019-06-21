package cm.tf.netty.io.myNio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class MyBioClientDemo {

    public static void main(String[] args) throws IOException {
        Socket client = new Socket("localhost", 8080);
        OutputStream os = client.getOutputStream();
        //生成一个随机的ID
        String name = UUID.randomUUID().toString();
        System.out.println("客户端发送数据：" + name);
        os.write(name.getBytes());
        os.close();
        client.close();
    }
}
