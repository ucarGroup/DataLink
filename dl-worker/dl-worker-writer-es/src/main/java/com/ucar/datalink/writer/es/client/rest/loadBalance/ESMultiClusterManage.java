package com.ucar.datalink.writer.es.client.rest.loadBalance;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.vo.ClusterVo;
import com.ucar.datalink.writer.es.exception.ESConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Description: 多es集群管理
 * 监控集群类，通过定时访问，监控集群的健康状态
 * 动态更新地址列表
 * All Rights Reserved.
 * Created on 2016-7-26 下午2:22:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class ESMultiClusterManage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ESMultiClusterManage.class);
	
	//调度器
	private static ScheduledExecutorService scheduledExecutorService;
	
	public static final String DEFAULT_CLUSTER_NAME = "default";
	
	private static Map<String,ESCluster> MC_MAP = new ConcurrentHashMap<String,ESCluster>();
	
	private volatile static boolean isStartMonitor = false;
	
	/**
	 * 
	 * Description: 启动监控线程
	 * Created on 2016-7-27 下午3:51:51
	 * @author  孔增（kongzeng@zuche.com）
	 */
	private static void startMonitor() {
		
		if(MC_MAP.size() == 0 || isStartMonitor) {
			return;
		}
		
		//初始化调度线程
		scheduledExecutorService =  Executors.newSingleThreadScheduledExecutor();
		//启动监控线程
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	monitorCluster();
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
		
		isStartMonitor = true;
	}

	/**
	 * 
	 * Description: 添加es配置，若已存在则将覆盖
	 * Created on 2016-7-26 下午5:18:10
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public synchronized static void addESConfigs(List<ESConfigVo> list) {
		
		if(list == null || list.size() == 0) {
			return;
		}
		for(ESConfigVo ev : checkESConfig(list)) {
			MC_MAP.put(ev.getClusterName(), new ESCluster(ev));
		}
		
		startMonitor();
		
	}
	/**
	 * 校验配置信息
	 * @param list
	 * @return
	 */
	private static List<ESConfigVo> checkESConfig(List<ESConfigVo> list) {
		Set<String> set = new HashSet<String>();
		List<ESConfigVo> newList = new ArrayList<ESConfigVo>();
		for(ESConfigVo config : list) {
			if(config.getIsUsed()) {
				if(!set.add(config.getClusterName())) {
					LOGGER.error(config.getClusterName() + " cluster has more one valid config , please check the config");
					throw new ESConfigException("es config check fail");
				}
				newList.add(config);
			}
		}
		return newList;
	}
	
	/**
	 * 
	 * Description: 校验所有es集群
	 * Created on 2016-7-27 上午10:23:26
	 * @author  孔增（kongzeng@zuche.com）
	 */
	private static void monitorCluster() {
		Iterator<Entry<String, ESCluster>> ito = MC_MAP.entrySet().iterator();
		while(ito.hasNext()){
			ESCluster mc = ito.next().getValue();
			if(mc == null) {
				continue;
			}
			for(Cluster c : mc.getClusterList()) {
				try{
					
					List<String> hosts = new ArrayList<String>();
					
					String host = c.getHost()+":"+c.getHttpPort();
					ClusterVo vo = new ClusterVo(mc.getClusterName());
					vo.setHost(host);
					vo.setMessageType("_cluster/state/nodes");
					String json = EsClient.getClusterMessage(vo);
					
					JSONObject jo = null;
					try {
						jo = JSONObject.parseObject(json);
					} catch (Exception e) {
						LOGGER.error("校验集群{}时,从{}获取集群信息后解析json串{}异常", mc.getClusterName(),host,json);
						continue;
					}
					if(jo == null) {
						LOGGER.error("校验集群{}时,从{}获取集群信息失败，将从集群其他结点获取", mc.getClusterName(),host);
						continue;
					}
					JSONObject nodes = jo.getJSONObject("nodes");
					
					Set<Entry<String, Object>> set = nodes.entrySet();
					Iterator<Entry<String, Object>> ite = set.iterator();
					while(ite.hasNext()){
						Map.Entry<String, Object> entry = ite.next();
						Object value = entry.getValue();
						if(value instanceof JSONObject){
							Object ip = ((JSONObject) value).get("transport_address");
							String transportAddress = String.valueOf(ip);
							
							hosts.add(transportAddress.substring(0, transportAddress.indexOf(":")+1) + c.getHttpPort());
						}
						
					}
					if(hosts.size() > 0){
						mc.setUrls(hosts) ;
						LOGGER.info(mc.getClusterName()+":"+hosts.toString());
					}
					
					break ;
					
				}catch(Throwable e){
					LOGGER.error(mc.getClusterName()+":monitor fail", e);
				}
			}
		}
		
	}

	/**
	 * 
	 * Description: 根据集群名称获取集群信息
	 * Created on 2016-7-27 上午11:45:43
	 * @author  孔增（kongzeng@zuche.com）
	 * @param clusterName
	 * @return
	 */
	public static ESCluster getMonitorCluster(String clusterName) {
		return MC_MAP.get(clusterName);
	}
	
}
