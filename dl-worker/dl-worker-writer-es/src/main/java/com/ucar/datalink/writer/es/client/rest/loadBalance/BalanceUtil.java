/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.loadBalance;

import java.util.List;
import java.util.Random;

/**
 * @author lihongbo
 * 
 * 负载工具类
 *
 */
public class BalanceUtil {
	
	/**
	 * 使用“随机”的负载算法，生成URL 
	 * @return
	 */
	public static String getRandomHost(ESCluster mc){
		
		List<String> list = mc.getUrls();
		if(list.size() > 0){
			Random random = new Random();
			int size = random.nextInt(list.size());
			return list.get(size);
		}
		
		return null ;
	}
	
}
