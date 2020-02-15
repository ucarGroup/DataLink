package com.ucar.datalink.biz.meta;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.event.HBaseRange;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.HbaseStatus;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.vo.HBaseParameterVO;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/7/4.
 */
public class HBaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(HBaseUtil.class);

    private static final String NULLJSON = "{}";
    /**
     * 这个url对应的是worker端机器暴露的rest接口地址，用来获取hbase所有的表
     */
    private static final String GET_TABLES_URL = "/hbase/getTables";
    /**
     * 这个url对应的是worker端机器暴露的rest接口地址，用来获取hbase某张表中的所有列元信息
     */
    private static final String GET_COLUMNS_URL = "/hbase/getColumns";
    /**
     * 检查当前连接是否正常
     */
    private static final String CHECK_CONNECTION = "/hbase/check/";
    /**
     * 获取一个表下的region数量
     */
    private static final String GET_REGION_COUNT = "/hbase/count";
    /**
     * 将表分切分成指定的region，并返回对应的startKye和endKey
     */
    private static final String GENERATE_SPLIT_INFO = "/hbase/split";
    /**
     * 指定读取若干条数据后解析表结构
     */
    private static final AtomicInteger HBASE_SPECIFIED_NUM = new AtomicInteger();
    /**
     * 这个url对应的是worker端机器暴露的rest接口地址，用来获取集群的状态信息
     */
    private static final String GET_STATUS_URL = "/hbase/status";
    private static WorkerService service;
    private static MediaDAO dao;

    static {
        //HBase工具类需要调用远端的worker机器(随机调用一台)，所以需要WorkerService的实现类
        //此外HBaseSrcParameter只包含了zk的znode，所以需要根据保存的zk id再去数据库中获取对应的ZKSrcParameter，这需要MediaDAO实现
        service = DataLinkFactory.getObject(WorkerService.class);
        dao = DataLinkFactory.getObject(MediaDAO.class);
    }

    /**
     * 根据传入的MediaSourceInfo 获取所有表的元信息
     *
     * @param info
     * @return
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) {
        checkHbase(info);
        HBaseMediaSrcParameter parameter = info.getParameterObj();
        long zkId = parameter.getZkMediaSourceId();
        String znode = parameter.getZnodeParent();
        MediaSourceInfo zkInfo = dao.findMediaSourceById(zkId);
        checkZookeepr(zkInfo);
        ZkMediaSrcParameter zkParameter = zkInfo.getParameterObj();

        HBaseParameterVO vo = new HBaseParameterVO();
        vo.setTableName("");
        vo.setZkAddress(zkParameter.getServers());
        vo.setPort(zkParameter.parsePort() + "");
        vo.setZnode(parameter.getZnodeParent());

        String json = execute(vo, GET_TABLES_URL);
        return JSONObject.parseArray(json, MediaMeta.class);
    }


    /**
     * 根据传入的MediaSourceInfo和表名，获取这个表下的所有列的元信息
     *
     * @param info
     * @param tableName
     * @return
     */
    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) {
        checkHbase(info);
        HBaseMediaSrcParameter parameter = info.getParameterObj();
        long zkId = parameter.getZkMediaSourceId();
        String znode = parameter.getZnodeParent();
        MediaSourceInfo zkInfo = dao.findMediaSourceById(zkId);
        checkZookeepr(zkInfo);
        ZkMediaSrcParameter zkParameter = zkInfo.getParameterObj();

        HBaseParameterVO vo = new HBaseParameterVO();
        vo.setTableName(tableName);
        vo.setZkAddress(zkParameter.getServers());
        vo.setPort(zkParameter.parsePort() + "");
        vo.setZnode(parameter.getZnodeParent());

        if (HBASE_SPECIFIED_NUM.get() > 0) {
            vo.setOnceFethAmount(HBASE_SPECIFIED_NUM.get());
        }
        String json = execute(vo, GET_COLUMNS_URL);
        if (json.equals(NULLJSON)) {
            throw new ErrorException(CodeContext.HBASE_COLUMNMETA_ERROR_CODE);
        }
        return JSONObject.parseArray(json, ColumnMeta.class);
    }

    public static int getRegionCount(MediaSourceInfo info, String tableName) {
        checkHbase(info);
        HBaseMediaSrcParameter parameter = info.getParameterObj();
        long zkId = parameter.getZkMediaSourceId();
        String znode = parameter.getZnodeParent();
        MediaSourceInfo zkInfo = dao.findMediaSourceById(zkId);
        checkZookeepr(zkInfo);
        ZkMediaSrcParameter zkParameter = zkInfo.getParameterObj();

        HBaseParameterVO vo = new HBaseParameterVO();
        vo.setTableName(tableName);
        vo.setZkAddress(zkParameter.getServers());
        vo.setPort(zkParameter.parsePort() + "");
        vo.setZnode(parameter.getZnodeParent());
        String json = execute(vo, GET_REGION_COUNT);
        int result = -1;
        try {
            result = Integer.parseInt(json);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    public static List<HBaseRange> generateHBaseSplitInfo(MediaSourceInfo info, String tableName, int splitCount) {
        checkHbase(info);
        HBaseMediaSrcParameter parameter = info.getParameterObj();
        long zkId = parameter.getZkMediaSourceId();
        String znode = parameter.getZnodeParent();
        MediaSourceInfo zkInfo = dao.findMediaSourceById(zkId);
        checkZookeepr(zkInfo);
        ZkMediaSrcParameter zkParameter = zkInfo.getParameterObj();

        HBaseParameterVO vo = new HBaseParameterVO();
        vo.setTableName(tableName);
        vo.setZkAddress(zkParameter.getServers());
        vo.setPort(zkParameter.parsePort() + "");
        vo.setZnode(parameter.getZnodeParent());
        vo.setSplitCount(splitCount);

        String json = execute(vo, GENERATE_SPLIT_INFO);
        Map<String, Object> map = JSONObject.parseObject(json, Map.class);
        JSONArray array = (JSONArray) map.get("range");
        Object[] obj_arr = array.toArray();
        List<HBaseRange> list = new ArrayList<>();
        for (Object o : obj_arr) {
            String str = JSONObject.toJSONString(o);
            HBaseRange hr = JSONObject.parseObject(str, HBaseRange.class);
            list.add(hr);
        }
        return list;
    }


    /**
     * 随机选取一个worker机器的ip
     *
     * @return
     */
    private static String getWorkAddress() {
        List<WorkerInfo> list = service.getList();
        Random rand = new Random();
        if (list == null || list.size() == 0) {
            throw new RuntimeException("list is emtpy");
        }
        int index = Math.abs(rand.nextInt(list.size()));
        WorkerInfo info = list.get(index);
        String address = info.getWorkerAddress();
        Integer port = info.getRestPort();
        String address_and_port = address + ":" + port;
        return address_and_port;
    }


    /**
     * 检查当前的MediaSourceInfo所包含的类似是否是HBase的
     *
     * @param info
     */
    private static void checkHbase(MediaSourceInfo info) {
        MediaSrcParameter parameter = info.getParameterObj();
        if (!(parameter instanceof HBaseMediaSrcParameter)) {
            throw new RuntimeException("当前的MediaSrcParameter类型错误 " + parameter);
        }
    }

    /**
     * 检查当前的MediaSourceInfo所包含的类似是否是zookeeper的
     *
     * @param info
     */
    private static void checkZookeepr(MediaSourceInfo info) {
        MediaSrcParameter parameter = info.getParameterObj();
        if (!(parameter instanceof ZkMediaSrcParameter)) {
            throw new RuntimeException("当前的MediaSrcParameter类型错误 " + parameter);
        }
    }


    /**
     * 执行一个HTTP请求获取元数据的json信息
     *
     * @param vo
     * @param path
     * @return
     */
    private static String execute(HBaseParameterVO vo, String path) {
        String json = JSONObject.toJSONString(vo);
        String address = getWorkAddress();
        String url = "http://" + address + path;
        String result = URLConnectionUtil.retryPOST(url, json);
        return result;
    }


    public static List<String> checkTargetTables(MediaSourceInfo realTargetMediaSourceInfo, Set<String> targetTableNameSet) {
        List<String> returnList = Lists.newArrayList();
        List<MediaMeta> mediaMetaList = getTables(realTargetMediaSourceInfo);
        List<String> tableNameList = mediaMetaList.stream().map(m -> {
            return m.getName();
        }).collect(Collectors.toList());
        Iterator<String> it = targetTableNameSet.iterator();
        while (it.hasNext()) {
            String targetTableName = it.next();
            if (!tableNameList.contains(targetTableName)) {
                returnList.add(targetTableName);
            }
        }
        return returnList;
    }

    public static void howManyPieceOfDataReadToParse(int sepcifiedAmount) {
        HBASE_SPECIFIED_NUM.compareAndSet(HBASE_SPECIFIED_NUM.get(), sepcifiedAmount);
    }

    public static HbaseStatus getClusterStatus(MediaSourceInfo mediaSourceInfo) {
        checkHbase(mediaSourceInfo);
        HBaseMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
        long zkId = parameter.getZkMediaSourceId();
        String znode = parameter.getZnodeParent();
        MediaSourceInfo zkInfo = dao.findMediaSourceById(zkId);
        checkZookeepr(zkInfo);
        ZkMediaSrcParameter zkParameter = zkInfo.getParameterObj();

        HBaseParameterVO vo = new HBaseParameterVO();
        vo.setTableName("");
        vo.setZkAddress(zkParameter.getServers());
        vo.setPort(zkParameter.parsePort() + "");
        vo.setZnode(parameter.getZnodeParent());

        String json = execute(vo, GET_STATUS_URL);

        logger.info("返回的hbase集群状态信息:" + json);
        return JSONObject.parseObject(json, HbaseStatus.class);
    }

}
