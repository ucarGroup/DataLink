package com.ucar.datalink.writer.es.intercept;

import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.es.client.rest.del.DelDoc;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.SimpleDocVo;
import com.ucar.datalink.writer.es.util.EsConfigManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * EsRouting场景
 *      专车改派单时，因司机变更，则es会有两条数据，需删除原来的司机数据
 *      该拦截器主表、子表都需要监控，只要routing字段值发生变更，就要删除es数据
 */
public class EsRoutingInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(EsRoutingInterceptor.class);
    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {

        if (record.getEventType() == EventType.UPDATE) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
            String esRouting = mappingInfo.getEsRouting();
            String[] esRoutingArr = esRouting.split(",");
            String tableName = mappingInfo.getTargetMediaName();
            if (!tableName.contains(".")) {
                throw new ValidationException("Please specify the index and type");
            }
            for(String columnName : esRoutingArr){
                //旧值和新值不一致，就认为是改派了
                if(StringUtils.isNotBlank(record.getOldColumn(columnName).getColumnValue()) && (!StringUtils.equals(record.getOldColumn(columnName).getColumnValue(),record.getColumn(columnName).getColumnValue()))){
                    DelDoc delDoc = DelDoc.getInstance();
                    SimpleDocVo simpleDocVo = new SimpleDocVo(EsConfigManager.getESConfig(mappingInfo.getTargetMediaSource(), mappingInfo.getTaskId()).getClusterName());
                    simpleDocVo.setIndex(StringUtils.substringBefore(tableName, "."));
                    simpleDocVo.setType(StringUtils.substringAfter(tableName, "."));
                    //聚合列
                    EventColumn joinColumn = record.getColumn(mappingInfo.getJoinColumn());
                    String joinColumnValue = "";
                    if(joinColumn == null){
                        List<EventColumn> keysColumn = record.getKeys();
                        if (!CollectionUtils.isEmpty(keysColumn)){
                            for (EventColumn column : keysColumn) {
                                if (column.getColumnName().equals(mappingInfo.getJoinColumn())) {
                                    joinColumnValue = column.getColumnValue();
                                    break;
                                }
                            }
                        }
                    }else{
                        joinColumnValue = joinColumn.getColumnValue();
                    }
                    //此id对应es里面的_id字段，值就是聚合列的值
                    simpleDocVo.setId(joinColumnValue);
                    //拼接routing
                    String esRoutingValue = "";
                    for(String columnNameTemp : esRoutingArr){
                        EventColumn eventColumn = record.getColumn(columnNameTemp);
                        if(eventColumn.isUpdate()){
                            esRoutingValue += record.getOldColumn(columnNameTemp).getColumnValue() + ",";
                        }else{
                            esRoutingValue += eventColumn.getColumnValue() + ",";
                        }
                    }
                    //去掉最后的逗号
                    esRoutingValue = esRoutingValue.substring(0,esRoutingValue.length() - 1);
                    simpleDocVo.setRoutingValue(esRoutingValue);

                    //删除
                    CRDResultVo crdResultVo =  delDoc.deleteDoc(simpleDocVo);
                    //删除不成功
                    if(!crdResultVo.isSuccess()){
                        throw new DataLoadException("elasticsearch delete data failed , the reason is :" + crdResultVo.getJsonString());
                    }
                    break;
                }
            }
        }

        return record;
    }

}
