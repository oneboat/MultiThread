package cn.tf.thread.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 探针数据接收
 *
 */
@Controller
public class DataReviceController {

	
	private Logger logger = LoggerFactory.getLogger(ProbeController2.class);
	
	@Autowired
	private ProbeDataService ProbeDataService;
	@Autowired
    private StringRedisTemplate redisTemplate;
	
	@RequestMapping("/data/recive")
	@ResponseBody
	public Object recive(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("开始接收探针数据--");
		String probeMac = request.getParameter("mac");
		String data = request.getParameter("data");
		System.out.println("探针Mac:"+probeMac);
		try {
			Enumeration<String> enums = request.getParameterNames();
			StringBuffer info = new StringBuffer();
			while (enums.hasMoreElements()) {
				String name = enums.nextElement();
				info.append(name ).append("=").append(request.getParameter(name)).append("&");
			}
			logger.info(info.toString());
			String type = request.getParameter("type");
			
			String lon = request.getParameter("lon");
			String lat = request.getParameter("lat");
			logger.info("探针坐标信息:"+probeMac+"|"+lon+"|"+lat);
			
			new Thread(() -> {
				saveRedis(probeMac,lon,lat);
			},"save_Redis").start();
			new Thread(() -> {
				saveData(type,data,probeMac,lon,lat);
			},"save_Data").start();
			new Thread(() -> {
				analysis();
			},"analysis").start();
			System.out.println("结束接收探针数据--");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "OK";
	}
	
	private void analysis() {
		System.out.println("客流人数分析");
		
	}

	private void saveData(String type, String data, String probeMac,String lon,String lat) {
		if ("new".equals(type)&& null != data && !"".equals(data)) {
			ProbeDataService.newReport(probeMac,data,null==lon|| isEmpty(lon)?"114.509835":lon,null==lat||isEmpty(lat)?"22.598219":lat);
		}else if ("leave".equals(type)&& null != data && !"".equals(data)) {
			ProbeDataService.leaveReport(probeMac,data);
		}
	}

	private void saveRedis(String probeMac,String lon,String lat) {
		//将坐标信息写入redis
		redisTemplate.opsForHash().put(CacheConstant.KEY_DEVICE_ADDRESS+":"+probeMac,"lon",null==lon?"0000":lon);
		redisTemplate.opsForHash().put(CacheConstant.KEY_DEVICE_ADDRESS+":"+probeMac,"lat",null==lat?"0000":lat);	
	}
	public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
	
}
