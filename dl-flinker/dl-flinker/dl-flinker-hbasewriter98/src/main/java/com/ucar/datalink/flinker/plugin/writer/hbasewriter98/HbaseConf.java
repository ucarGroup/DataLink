package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * Hbase Configuration管理器
 * 
 * @author lubiao
 *
 */
public class HbaseConf {
	private static Logger LOG = LoggerFactory.getLogger(HbaseConf.class);
	
	private static volatile org.apache.hadoop.conf.Configuration conf;

	public static org.apache.hadoop.conf.Configuration getHbaseConf(String hbaseConf) {
		if (conf == null) {
			synchronized (HbaseUtil.class) {
				if (conf == null) {
					conf = parseConfiguration(hbaseConf);
				}
			}
		}
		return conf;
	}

	private static org.apache.hadoop.conf.Configuration parseConfiguration(String hbaseConf) {
		if (StringUtils.isBlank(hbaseConf)) {
			throw DataXException.asDataXException(HBaseWriter98ErrorCode.REQUIRED_VALUE,
					"写Hbase 时需要配置 hbaseConfig，其内容为 Hbase 连接信息，请联系 Hbase PE 获取该信息.");
		}
		org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();

		try {
			Map<String, String> map = JSON.parseObject(hbaseConf, new TypeReference<Map<String, String>>() {
			});

			// / 用户配置的 key-value 对 来表示 hbaseConf
			Validate.isTrue(map != null, "hbaseConfig 不能为空 Map 结构!");
			for (Map.Entry<String, String> entry : map.entrySet()) {
				conf.set(entry.getKey(), entry.getValue());
			}
			return conf;
		} catch (Exception e) {
			// 用户配置的 hbase 配置文件路径
			LOG.warn("尝试把您配置的 hbaseConfig: {} \n 当成 json 解析时遇到错误:", e);
			LOG.warn("现在尝试把您配置的 hbaseConfig: {} \n 当成文件路径进行解析.", hbaseConf);
			conf.addResource(new Path(hbaseConf));

			LOG.warn("您配置的 hbaseConfig 是文件路径, 是不推荐的行为:因为当您的这个任务迁移到其他机器运行时，很可能出现该路径不存在的错误. 建议您把此项配置改成标准的 Hbase 连接信息，请联系 Hbase PE 获取该信息.");
			return conf;
		}
	}
}
