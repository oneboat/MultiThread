package cn.tf.thread.lesson7.work;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//在景区客流监测中，当客流人数超过阈值时，触发消息通知，
//1、先把预警信息保存到数据库中，
// 2、发送短信通知到相应的景区负责人
public class App {

    public static void main(String[] args) throws InterruptedException {

        WarnService warnService= new WarnService();
        warnService.start();
        for(int i=0;i<20;i++){
            Map<String,Object> map = generateData();
            warnService.checkWarnInfo(map);
        }
        Thread.sleep(2000);
        warnService.destory();
    }

    public static Map<String,Object> generateData(){
        Map<String,Object> map= new HashMap<>();
        Random random = new Random();
        int num = random.nextInt(20000) +1;
        map.put("num",num);
        return map;
    }

}
