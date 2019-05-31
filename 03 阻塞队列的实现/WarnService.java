package cn.tf.thread.lesson7.work;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class WarnService extends Thread{
    private LinkedBlockingQueue queue = new LinkedBlockingQueue(10);

    public void destory(){
        super.interrupt();
    }

    @Override
    public void run() {
        init();
    }


    public void init(){
        while(true){
            try {
                Object o=queue.take();
                System.out.println("从阻塞队列中获取到的数据是:"+o);
                sendMsg(o);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * 检查客流人数是否超过阈值，如果超过就存库病发送短信
     * @return
     */
    public boolean checkWarnInfo(Object o){
        Map<String,Object> info = (Map<String,Object> ) o;
        if(Integer.parseInt(info.get("num").toString())>10000){
            System.out.println("人数"+info.get("num")+"超过阈值了!");
            saveToMysql(o);
            queue.add(o);
            return true;
        }
        System.out.println("人数"+info.get("num")+"，正常!");
        return false;
    }

    /**
     * 将数据保存到数据库中
     * @param o
     * @return
     */
    public boolean saveToMysql(Object o){
        try {
            //TODO,执行保存的业务逻辑
            System.out.println("执行sql，将数据保存到数据库中");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 给景区负责人发送短信
     * @param o
     * @return
     */
    public boolean sendMsg(Object o){
        try {
            //TODO,给景区负责人发送短信
            System.out.println("给景区负责人发送短信");
            System.out.println("========================================");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
