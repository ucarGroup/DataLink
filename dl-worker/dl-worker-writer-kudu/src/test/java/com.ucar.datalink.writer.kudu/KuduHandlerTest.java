/*
package com.ucar.datalink.writer.kudu;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kudu.KuduWriterParameter;
import com.ucar.datalink.writer.kudu.handle.RdbEventRecordHandler;
import org.junit.Test;

import java.util.ArrayList;

public class KuduHandlerTest extends RdbEventRecordHandler {


    @Test
    public void test(){
        ArrayList<RdbEventRecord> rdbEventRecords = new ArrayList<>();
        RdbEventRecord rdbEventRecord = new RdbEventRecord();
        rdbEventRecord.setTableName("t_dl_test_xxx");//目标表名字
        rdbEventRecord.setSchemaName("source_test_database");


        MediaMappingInfo mediaMappingInfo = new MediaMappingInfo();
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();

        mediaSourceInfo.setId(100L);

        KuduMediaSrcParameter kuduMediaSrcParameter = new KuduMediaSrcParameter();
        kuduMediaSrcParameter.setDatabase("impala::kudu_test");
        ArrayList<KuduMediaSrcParameter.KuduMasterConfig> kuduMasterConfigs = new ArrayList<>();
        kuduMasterConfigs.add(new  KuduMediaSrcParameter.KuduMasterConfig("10.104.132.72",7051));
        kuduMasterConfigs.add(new  KuduMediaSrcParameter.KuduMasterConfig("10.104.132.73",7051));
        kuduMasterConfigs.add(new  KuduMediaSrcParameter.KuduMasterConfig("10.104.132.75",7051));
        kuduMasterConfigs.add(new  KuduMediaSrcParameter.KuduMasterConfig("10.104.132.223",7051));
        kuduMasterConfigs.add(new  KuduMediaSrcParameter.KuduMasterConfig("10.104.132.221",7051));
        kuduMediaSrcParameter.setKuduMasterConfigs(kuduMasterConfigs);


        rdbEventRecord.metaData().put("MediaMapping", mediaMappingInfo);
        rdbEventRecord.setEventType(EventType.INSERT);

        ArrayList<EventColumn> eventColumns = new ArrayList<>();
        EventColumn eventColumn = new EventColumn();
        eventColumn.setColumnName("name");
        eventColumn.setColumnValue("99999999");
        eventColumns.add(eventColumn);
        rdbEventRecord.setColumns(eventColumns);

        EventColumn keyEventColumn  = new EventColumn();
        keyEventColumn.setColumnName("name");
        keyEventColumn.setColumnValue("99999999");
        ArrayList<EventColumn> keys = new ArrayList<>();
        keys.add(keyEventColumn);
        rdbEventRecord.setKeys(keys);

        rdbEventRecords.add(rdbEventRecord);

        mediaMappingInfo.setTargetMediaSource(mediaSourceInfo);
        mediaSourceInfo.setParameter(JSONObject.toJSONString(kuduMediaSrcParameter));
        PluginWriterParameter pluginWriterParameter = new KuduWriterParameter();
////        TaskWriterContext taskWriterContext = new WorkerTaskWriterContext(null, pluginWriterParameter);
//
//        this.doWrite(rdbEventRecords,taskWriterContext);

    }

}
*/
