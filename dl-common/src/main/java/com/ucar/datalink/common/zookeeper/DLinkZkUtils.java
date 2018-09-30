package com.ucar.datalink.common.zookeeper;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.commons.lang.StringUtils;

/**
 * Created by lubiao on 2016/12/8.
 */
public class DLinkZkUtils {
    private ZkClientX zkClient;
    private String zkRoot;

    private DLinkZkUtils() {
    }

    public String zkRoot() {
        if (StringUtils.isEmpty(zkRoot)) {
            throw new IllegalStateException("get zkRoot failed,should initial DLinkZkUtils first.");
        }
        return zkRoot;
    }

    public ZkClientX zkClient() {
        if (zkClient == null) {
            throw new IllegalStateException("get zkClient failed,should initial DLinkZkUtils first.");
        }
        return zkClient;
    }

    //--------------------------------------- Static Fields & Methods ---------------------------------------------

    private static DLinkZkUtils zkUtils;

    public static synchronized DLinkZkUtils init(ZkConfig zkConfig, String zkRoot) {
        if (zkUtils != null) {
            throw new DatalinkException("ZkUtils is already initialized,can't initial again.");
        }

        zkUtils = new DLinkZkUtils();
        zkUtils.zkRoot = zkRoot;
        zkUtils.zkClient = ZkClientX.getZkClient(zkConfig);
        return zkUtils;
    }

    public static DLinkZkUtils get() {
        if (zkUtils == null) {
            throw new IllegalStateException("get ZkUtils failed,should initial first.");
        }
        return zkUtils;
    }
}
