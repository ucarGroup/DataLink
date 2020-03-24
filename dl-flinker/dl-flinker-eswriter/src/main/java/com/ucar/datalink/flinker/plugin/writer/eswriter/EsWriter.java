package com.ucar.datalink.flinker.plugin.writer.eswriter;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.spi.Writer;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.loadBalance.ESMultiClusterManage;
import com.ucar.datalink.flinker.plugin.writer.eswriter.exception.VersionConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ES writer
 *
 * Created by yw.zhang02 on 2016/7/25.
 */
public class EsWriter extends Writer {

    private static final Logger logger = LoggerFactory.getLogger(EsWriter.class);

    public static class Job extends Writer.Job {

        private Configuration originalConfig = null;

        //写插件获得writer部分的配置
        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();

            //校验参数
            validateParameter();
            //初始化需要合并的列
            EsHelper.initMergeColumn(originalConfig);
        }

        private void validateParameter() {

            List<Configuration> columns = this.originalConfig.getListConfiguration(Key.COLUMN);

            if (null == columns || columns.size() == 0) {
                throw DataXException.asDataXException(EsWriterErrorCode.REQUIRED_VALUE, "您需要指定 columns");
            }
            //es基础配置不为空
            this.originalConfig.getNecessaryValue(Key.ES_USERNAME, EsWriterErrorCode.REQUIRED_VALUE);
            this.originalConfig.getNecessaryValue(Key.ES_PASSWORD, EsWriterErrorCode.REQUIRED_VALUE);
            this.originalConfig.getNecessaryValue(Key.ES_HOSTS, EsWriterErrorCode.REQUIRED_VALUE);
            this.originalConfig.getNecessaryValue(Key.ES_HTTP_PORT, EsWriterErrorCode.REQUIRED_VALUE);
            this.originalConfig.getNecessaryValue(Key.ES_TCP_PORT, EsWriterErrorCode.REQUIRED_VALUE);
        }


        //全局准备工作 比如odpswriter清空目标表。
        @Override
        public void prepare() {
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {

            List<Configuration> configurationList = new ArrayList<Configuration>();

            for(int i=0; i< mandatoryNumber; i++){
                Configuration configuration = this.originalConfig.clone();
                configurationList.add(configuration);
            }

            return configurationList;
        }
        //全局后置工作 比如mysqlwriter同步完影子表后的rename操作。
        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }
    }

    public static class Task extends Writer.Task {

        private static final Logger LOG = LoggerFactory.getLogger(Task.class);

        private static Integer DEFAULT_RETRY_TIMES = 3;

        private static Integer DEFAULT_BATCH_SIZE = 1;

        private Configuration writerSliceConfig;

        private List<Object> columns;

        private int columnNumber;

        private String esIndex;

        private String esType;

        private String table;

        private Integer batchSize;

        private Integer retryTimes;

        private OperateType operateType;

        private Boolean isAddTablePrefix;

        //es routing支持
        private String esRouting;
        
        private String esRoutingIgnore;
        
        
        @Override
        public void init() {
            this.writerSliceConfig = super.getPluginJobConf();

            this.columns = this.writerSliceConfig.getList(Key.COLUMN);

            this.columnNumber = columns.size();

            this.esIndex = writerSliceConfig.getString(Key.ES_INDEX);

            this.esType = writerSliceConfig.getString(Key.ES_TYPE);

            this.table = writerSliceConfig.getString(Key.TABLE);

            this.isAddTablePrefix = writerSliceConfig.getBool(Key.ES_IS_ADD_TABLE_PREFIX,true);

            this.esRouting = writerSliceConfig.getString(Key.ES_ROUTING);
            
            this.esRoutingIgnore = writerSliceConfig.getString(Key.ES_ROUTING_IGNORE);
 
            
            if(this.table == null){
                this.table = esType;
            }

            //操作类型
            String operateUpperStr = writerSliceConfig.getString(Key.OPERATE_TYPE).toUpperCase();
            String tmp = OperateType.UPSERT.getValue();

            if(operateUpperStr == null || OperateType.INSERT.getValue().equals(operateUpperStr)){
                operateType = OperateType.INSERT;
            }else if (OperateType.UPSERT.getValue().equals(operateUpperStr)){
                operateType = OperateType.UPSERT;
            }else {
                operateType = OperateType.UPDATE;
            }

            this.batchSize = writerSliceConfig.getInt(Key.ES_BATCH_SIZE);
            if(this.batchSize == null || this.batchSize < 1){
                this.batchSize = DEFAULT_BATCH_SIZE;
            }

            this.retryTimes = writerSliceConfig.getInt(Key.RETRY_TIMES);
            if(this.retryTimes == null || this.retryTimes < 1){
                this.retryTimes = DEFAULT_RETRY_TIMES;
            }
            //初始化Es配置信息
            initEsConfig();
        }

        /**
         * 初始化es配置
         */
        private void initEsConfig(){
            String username = this.writerSliceConfig.getString(Key.ES_USERNAME);//用户
            String password = this.writerSliceConfig.getString(Key.ES_PASSWORD);//密码
            String hosts = this.writerSliceConfig.getString(Key.ES_HOSTS);//集群地址
            Integer httpPort = this.writerSliceConfig.getInt(Key.ES_HTTP_PORT);//http端口
            Integer tcpPort = this.writerSliceConfig.getInt(Key.ES_TCP_PORT);//tcp端口

            List<ESConfigVo> esConfigVoList = new ArrayList<ESConfigVo>();

            ESConfigVo esConfigVo = new ESConfigVo();
            esConfigVo.setUser(username);//
            esConfigVo.setPass(password);
            esConfigVo.setHosts(hosts);
            esConfigVo.setHttp_port(httpPort);
            esConfigVo.setTcp_port(tcpPort);
            esConfigVoList.add(esConfigVo);
            ESMultiClusterManage.addESConfigs(esConfigVoList);
        }

        @Override
        public void startWrite(RecordReceiver recordReceiver) {
            LOG.info("begin do write...");
            Record record;
            try {
                List<Record> recordList = new ArrayList<Record>();
                //terminal record为空
                while ((record = recordReceiver.getFromReader()) != null) {
                    recordList.add(record);

                    if (record.getColumnNumber() != this.columnNumber) {
                        // 源头读取字段列数与目的表字段写入列数不相等，直接报错
                        throw DataXException
                                .asDataXException(
                                        EsWriterErrorCode.CONFIG_INVALID_EXCEPTION,
                                        String.format(
                                                "列配置信息有错误. 因为您配置的任务中，源头读取字段数:%s 与 目的表要写入的字段数:%s 不相等. 请检查您的配置并作出修改.",
                                                record.getColumnNumber(),
                                                this.columnNumber));
                    }
                    //如果达到batchSize的值 则执行
                    if(recordList.size() >= batchSize){
                        flushRecordListWithTimeRecord(recordList);
                    }
                }
                //如果结束时缓冲区未满时执行
                if(recordList.size() > 0){
                    flushRecordListWithTimeRecord(recordList);
                }

            }catch (Exception e){
                LOG.error("", e);
                ErrorRecord.addError(e);
                throw new RuntimeException(e);
            }
            LOG.info("end do write");
        }


        private void flushRecordListWithTimeRecord(List<Record> recordList) {
            long startTime = System.currentTimeMillis();
            flushRecordList(recordList);
            long elapseTime = System.currentTimeMillis() - startTime;
            logger.info("flush record list ->"+elapseTime+" total->"+recordList.size());
        }

        private void flushRecordList(List<Record> recordList){

            //根据重试次数来进行
            for(int i=0; i<retryTimes; i++){
                try {
                    String batchResp;
                    if(this.operateType == OperateType.INSERT){
                        batchResp = EsHelper.batchCreateDoc(isAddTablePrefix,esIndex, esType, table, columns, recordList, esRouting, esRoutingIgnore);
                    } else if (this.operateType == OperateType.UPSERT){
                        batchResp = EsHelper.batchUpsertDoc(isAddTablePrefix,esIndex, esType, table, columns, recordList, esRouting, esRoutingIgnore);
                    }else {
                        batchResp = EsHelper.batchUpdateDoc(isAddTablePrefix,esIndex, esType, table, columns, recordList, esRouting, esRoutingIgnore);
                    }
                    //是否错误
                    if(!isBatchError(batchResp)){
                        break;
                    }
                    ErrorRecord.addError("返回信息:"+batchResp);
                    LOG.error("返回信息：{}", batchResp);
                }catch (Exception e){
                    if(i!=retryTimes-1){
                        try {
                            if(e instanceof VersionConflictException){
                                TimeUnit.SECONDS.sleep(5);
                            }else {
                                TimeUnit.SECONDS.sleep(1);
                            }
                        } catch (InterruptedException e1) {
                        }
                        continue;
                    }
                    LOG.error("", e);
                    ErrorRecord.addError(e);
                }

                throw new RuntimeException("flushRecord发生异常");
            }

            recordList.clear();
        }

        /**
         * 判断返回结果是否是错误的结果
         *  此逻辑为如果更新的记过不存在则认为是正常的并跳过
         * @param batchResp
         * @return
         */
        private boolean isBatchError(String batchResp) {
            Configuration batchRespConf = Configuration.from(batchResp);
            boolean error = batchRespConf.getBool("errors");
            if(error){//error 是否有错
                if(this.operateType == OperateType.UPDATE) {
                    List<Configuration> itemConfList = batchRespConf.getListConfiguration("items");
                    for (Configuration itemConf : itemConfList) {
                        Configuration updateConf = itemConf.getConfiguration("update");
                        Integer status = updateConf.getInt("status");

                        //200为成功，404为找不到记录也跳过,类似于数据库更新一条没有的记录
                        if(status !=200 && status !=201 && status !=404){
                            if(status == 409){
                                LOG.error(updateConf.toString());
                                throw new VersionConflictException();
                            }
                            return true;
                        }

                        if(status == 404){
                            LOG.error("@@@@@@ " + updateConf.toString());
                        }
                    }
                }else {
                    return true;
                }
            }
            return false;

        }

        @Override
        public void destroy() {

        }
    }
}
