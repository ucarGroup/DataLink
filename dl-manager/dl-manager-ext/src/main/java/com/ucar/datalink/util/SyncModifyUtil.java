package com.ucar.datalink.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.auto.modify.CheckFactory;
import com.ucar.datalink.biz.auto.modify.ICheck;
import com.ucar.datalink.biz.meta.*;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.spark.ModifyCheckColumnInfo;
import com.ucar.datalink.biz.spark.SyncModifyTableInfo;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.wang09 on 2018-05-17 17:49.
 */
public class SyncModifyUtil {

    private static Logger logger = LoggerFactory.getLogger(SyncModifyUtil.class);

    private static JobService jobService;

    private static MediaSourceService mediaSourceService;

    static {
        jobService = DataLinkFactory.getObject(JobService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
    }



    public static boolean checkModifyColumn(JobConfigInfo jobConfig) {
        try {
            if( !SyncUtil.isAdaptModify(jobConfig.getJob_content()) ) {
                return true;
            }
            long mediaSourceId = jobConfig.getJob_src_media_source_id();
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(mediaSourceId);
            MediaSourceInfo targetMediaSourceInfo = mediaSourceService.getById(jobConfig.getJob_target_media_source_id());

            ICheck check = CheckFactory.getModifyCheckType(mediaSourceInfo,targetMediaSourceInfo);
            check.check(mediaSourceInfo,targetMediaSourceInfo,jobConfig);
            return true;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }


    public static boolean checkModifyColumnWithoutOpen(JobConfigInfo jobConfig) {
        try {
            MediaSourceInfo mediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(jobConfig.getJob_src_media_source_id());
            MediaSourceInfo targetMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(jobConfig.getJob_target_media_source_id());
            ICheck check = CheckFactory.getModifyCheckType(mediaSourceInfo,targetMediaSourceInfo);
            check.check(mediaSourceInfo,targetMediaSourceInfo,jobConfig);
            return true;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }




    public static void modifyJsonColumn(JobConfigInfo info, ModifyCheckColumnInfo src, ModifyCheckColumnInfo dest)  {
        String json = info.getJob_content();
        json = modifyColumn(json,src,"job.content[0].reader.parameter.column");
        json = modifyColumn(json,dest,"job.content[0].writer.parameter.column");
        logger.debug(json);
        info.setJob_content(json);
        jobService.modifyJobConfigContent(info);
    }

    private static String modifyColumn(String json, ModifyCheckColumnInfo columnInfo, String path) {
        DLConfig connConf = DLConfig.parseFrom(json);
        if(columnInfo.getNameType().size() > 0) {
            JSONArray array = new JSONArray();
            for(ModifyCheckColumnInfo.NameType nt: columnInfo.getNameType()) {
                JSONObject obj = new JSONObject();
                obj.put("name",nt.name);
                obj.put("type",nt.type);
                array.add(obj);
            }
            connConf.set(path,array);
            logger.debug("modifyColumn -> "+connConf.toJSON());
        }
        else if(columnInfo.getName().size() > 0) {
            JSONArray array = new JSONArray();
            for(String s : columnInfo.getName()) {
                array.add(s);
            }
            connConf.set(path,array);
            logger.debug("modifyColumn -> "+connConf.toJSON());
        }
        else if(columnInfo.getIndexType().size() > 0) {
            JSONArray array = new JSONArray();
            int index = 0;
            for(ModifyCheckColumnInfo.IndexTye nt: columnInfo.getIndexType()) {
                JSONObject obj = new JSONObject();
                obj.put("index",""+index);
                obj.put("type",nt.type);
                array.add(obj);
                index++;
            }
            connConf.set(path,array);
            logger.debug("modifyColumn -> "+connConf.toJSON());
        }
        else {
            //抛错
            logger.warn("unknown state , ModifyCheckColumnInfo is empty");
        }
        return connConf.toJSON();
    }


    public static void sendModifyColumn(SyncModifyTableInfo info) {
        try {
            SyncModifyTableInfo[] modifyTableInfo = new SyncModifyTableInfo[1];
            modifyTableInfo[0] = info;
            String msg = JSONObject.toJSONString(modifyTableInfo);
            Map<String,String> kv = new HashMap<String,String>();
            kv.put("data",msg);
            String token = ConfigReadUtil.getString("datax.auto.cdse.token");
            kv.put("token",token);
            String url = ConfigReadUtil.getString("datax.auto.cdse.modify.url");
            String result = HttpUtils.post(url,kv);

            String modifyInfo = info.getType() +" "+ info.getDatabase() +" "+ info.getTable();
            logger.info("sendModifyColumn ["+ modifyInfo +"] result -> "+result);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            logger.error(info.toString());
        }
    }



    public static String parseColumnToHiveType(MediaMeta mm,ColumnMeta cm) {
        MediaMeta mediaMeta = new MediaMeta();
        mediaMeta.setDbType(mm.getDbType());
        mediaMeta.setName(mm.getName());
        mediaMeta.setNameSpace(mm.getNameSpace());
        List<ColumnMeta> list = new ArrayList<>();
        list.add(cm);
        mediaMeta.setColumn(list);
        MediaMeta newMediaMeta = MetaMapping.transformToHDFS(mediaMeta);
        return newMediaMeta.getColumn().get(0).getType();
    }


    public static List<ColumnMeta> getNewColumnInfo(MediaSourceInfo info, String name) throws Exception {
        if(info.getType()== MediaSourceType.MYSQL || info.getType()==MediaSourceType.SQLSERVER || info.getType()==MediaSourceType.POSTGRESQL) {
            return RDBMSUtil.getColumns(info, SyncUtil.parseJobMediaName(name));
        }
        else if(info.getType() == MediaSourceType.HDFS) {
            return HDFSUtil.getColumns(info, SyncUtil.parseJobMediaName(name) );
        }
        else if(info.getType() == MediaSourceType.HBASE) {
            return HBaseUtil.getColumns(info, SyncUtil.parseJobMediaName(name) );
        }
        else if(info.getType() == MediaSourceType.ELASTICSEARCH) {
            return ElasticSearchUtil.getColumns(info, SyncUtil.parseJobMediaName(name) );
        }
        else {
            throw new UnsupportedOperationException("unknown type "+info.getType());
        }
    }

    public static boolean compareMediaTypeForSrc(JobConfigInfo info, MediaSourceType type) {
        if(info==null) {
            return false;
        }
        long srcMediaSourceId = info.getJob_src_media_source_id();
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(srcMediaSourceId);
        if(mediaSourceInfo.getType() == type) {
            return true;
        }
        return false;
    }

    public static boolean compareMediaTypeForTarget(JobConfigInfo info, MediaSourceType type) {
        if(info==null) {
            return false;
        }
        MediaSourceInfo srcMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(info.getJob_src_media_source_id());
        MediaSourceInfo targetMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(info.getJob_target_media_source_id());
        if(targetMediaSourceInfo.getType()==type) {
            MediaSourceType srcType = srcMediaSourceInfo.getType();
            if(srcType==MediaSourceType.MYSQL || srcType==MediaSourceType.SQLSERVER) {
                return true;
            }
        }
        return false;
    }


}
