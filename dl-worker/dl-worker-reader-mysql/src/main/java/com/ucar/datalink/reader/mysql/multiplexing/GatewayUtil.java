package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 工具类
 * <p>
 * Created by lubiao on 2019/5/8.
 */
public class GatewayUtil {
    private static final Logger logger = LoggerFactory.getLogger(GatewayUtil.class);
    private static final String NAME_PREFIX = "multiplexing";

    private static LoadingCache<LoadingKey, GatewayInstance> instances = CacheBuilder.newBuilder().build(
            new CacheLoader<LoadingKey, GatewayInstance>() {
                @Override
                public GatewayInstance load(LoadingKey key) throws Exception {
                    String oldMDC = MDC.get(Constants.MDC_TASKID);
                    try {
                        MDC.put(Constants.MDC_TASKID, key.gwCanalName);

                        GatewayInstance instance = new GatewayInstance(key.gwCanal);
                        GatewayInstanceMonitor.register(key.gwCanalName, instance);
                        return instance;
                    } finally {
                        if (StringUtils.isNotBlank(oldMDC)) {
                            MDC.put(Constants.MDC_TASKID, oldMDC);
                        } else {
                            MDC.remove(Constants.MDC_TASKID);
                        }
                    }
                }
            }
    );

    public static GatewayInstance getGatewayInstance(Canal canal) {
        Canal gwCanal = cloneAndAdjustCanalConfig(canal);
        return instances.getUnchecked(new LoadingKey(gwCanal.getName(), gwCanal));
    }

    private static Canal cloneAndAdjustCanalConfig(Canal in) {
        Canal out = clone(in);

        out.setName(buildCanalName(out));
        //让parser自己自动生成slaveid
        out.getCanalParameter().setSlaveId(-1L);
        return out;
    }

    //浅拷贝
    private static Canal clone(Canal in) {
        Canal out;
        try {
            //序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(in);
            //反序列化
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            out = (Canal) ois.readObject();

        } catch (Throwable t) {
            logger.info("canal clone failed.", t);
            throw new DatalinkException("canal clone failed", t);
        }
        return out;
    }

    //ip+端口确定唯一性
    private static String buildCanalName(Canal canal) {
        List<List<CanalParameter.DataSourcing>> datasources = canal.getCanalParameter().getGroupDbAddresses();
        CanalParameter.DataSourcing master = datasources.get(0).get(0);
        InetSocketAddress socketAddress = master.getDbAddress();
        return NAME_PREFIX + "-" + socketAddress.getHostName() + "-" + socketAddress.getPort();
    }

    private static class LoadingKey {
        private String gwCanalName;
        private Canal gwCanal;

        public LoadingKey(String gwCanalName, Canal gwCanal) {
            this.gwCanalName = gwCanalName;
            this.gwCanal = gwCanal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LoadingKey that = (LoadingKey) o;
            return gwCanalName.equals(that.gwCanalName);
        }

        @Override
        public int hashCode() {
            return gwCanalName.hashCode();
        }
    }
}
