package com.ucar.datalink.writer.es.intercept;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.mapping.RDBMSMapping;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.relationship.SqlCheckColumnInfo;
import com.ucar.datalink.domain.relationship.SqlCheckItem;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.writer.es.client.rest.vo.MappingIndexVo;
import com.ucar.datalink.writer.es.util.EsConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/9/5.
 */
public class DdlEventInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(DdlEventInterceptor.class);

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        if (record.getEventType().isDdl()) {
            logger.info("receive a ddl record in DdlEventInterceptor.ddl sql is " + record.getSql());

            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);

            List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(mappingInfo.getSourceMedia().getMediaSource().getType(), record.getSql());
            if (holders.size() > 1) {
                throw new ValidationException("The count of ddl slqs is more than one,please check,it may be a bug.");
            }
            SQLStatementHolder holder = holders.get(0);
            holder.check();
            List<SqlCheckItem> sqlCheckItemList = holder.getSqlCheckItems();
            for (SqlCheckItem item : sqlCheckItemList) {
                List<SqlCheckColumnInfo> columnAddInfoList = item.getColumnsAddInfo();
                if (columnAddInfoList != null && columnAddInfoList.size() > 0) {
                    for (SqlCheckColumnInfo columnAdd : columnAddInfoList) {
                        String columnName = columnAdd.getName().replace("`", "");
                        String columnDataType = columnAdd.getDataType();
                        String esColumnName = columnName;
                        if (mappingInfo.isEsUsePrefix()) {
                            esColumnName = mappingInfo.getSourceMedia().getName() + "|" + columnName;
                        }

                        ColumnMeta columnMeta = new ColumnMeta();
                        columnMeta.setType(columnDataType);
                        RDBMSMapping rdbmsMapping = new RDBMSMapping();
                        ColumnMeta esColumnMeta = rdbmsMapping.toES(columnMeta);
                        String esColumnType = esColumnMeta.getType();

                        Map<String, String> columnMap = new HashMap<>();
                        columnMap.put("type", esColumnType);
                        if (esColumnType.equals("string")) {
                            columnMap.put("index", "not_analyzed");
                        }
                        if (esColumnType.equals("date")) {
                            columnMap.put("format", "yyyy-MM-dd HH:mm:ss");
                        }
                        Map<String, Object> columnObj = new HashMap<>();
                        columnObj.put(esColumnName, columnMap);
                        Map<String, Object> columnPro = new HashMap<>();
                        columnPro.put("properties", columnObj);
                        String columnJson = JSONObject.toJSONString(columnPro);
                        logger.info("columnJson:{}", columnJson);

                        String targetMediaName = mappingInfo.getTargetMediaName();
                        String[] array = targetMediaName.split("\\.");
                        MappingIndexVo vo =  new MappingIndexVo();
                        vo.setIndex(array[0]);
                        vo.setType(array[1]);
                        MediaSourceInfo targetMediaSource = mappingInfo.getTargetMediaSource();
                        ESConfigVo esConfigVo = EsConfigManager.getESConfig(targetMediaSource);
                        String hosts = esConfigVo.getHosts();
                        String[] hostArr = hosts.split(",");
                        Integer port = esConfigVo.getHttp_port();
                        String host = hostArr[0] + ":" +port;
                        vo.setHost(host);
                        vo.setClusterName(esConfigVo.getClusterName());
                        vo.setUser(esConfigVo.getUser());
                        vo.setPass(esConfigVo.getPass());
                        vo.setMetaType("_mapping");
                        logger.info("Url:{}", vo.getUrl());
                        try {
                            String response = EsClient.updateMappingIndex(vo, columnJson.getBytes("utf-8"));
                            logger.info("ES update mapping index succeed.response:{}", response);
                        } catch (UnsupportedEncodingException e) {
                            logger.info("ES update mapping index failed:", e);
                        }
                    }
                }
            }
            return null;
        }
        return record;
    }
}
