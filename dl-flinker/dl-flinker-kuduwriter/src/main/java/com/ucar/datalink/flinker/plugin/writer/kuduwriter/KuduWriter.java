package com.ucar.datalink.flinker.plugin.writer.kuduwriter;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.CommonErrorCode;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.spi.Writer;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.plugin.writer.kuduwriter.exception.KuduWriterErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.kudu.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.ucar.datalink.flinker.plugin.writer.kuduwriter.exception.KuduWriterErrorCode.CONFIG_INVALID_EXCEPTION;

public class KuduWriter extends Writer {

    private static final String TABLE_NAME_PATH = "table";
    private static final String MASTER_ADDRESSES_PATH = "master_addresses";
    private static final String COLUMNS_PATH = "column";
    private static final String RECORD_BUFFER_SIZE = "bufferSize";


    private static Logger LOG = LoggerFactory.getLogger(KuduWriter.class);


    public static class Job extends Writer.Job {
        public Configuration configuration;

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            this.configuration = super.getPluginJobConf();
            List<Configuration> taskConfiguration = new ArrayList<Configuration>();
            while (mandatoryNumber > 0) {
                mandatoryNumber = mandatoryNumber - 1;
                taskConfiguration.add(configuration.clone());
            }
            return taskConfiguration;
        }

        @Override
        public void init() {
            this.configuration = super.getPluginJobConf();
            if(LOG.isDebugEnabled()){
                LOG.info("job init config{}",this.configuration.toJSON());
            }
            verify();
        }

        @Override
        public void destroy() {
            //无需操作
        }

        /**
         * 校验表名和字段
         */
        private void verify() {
            String tableName = this.configuration.getString(TABLE_NAME_PATH);
            List<String> masterAddresses = this.configuration.getList(MASTER_ADDRESSES_PATH,String.class);
            List<String> columnList = this.configuration.getList(COLUMNS_PATH,String.class);
            Integer recodeSizeCommitBuffer = this.configuration.getInt(RECORD_BUFFER_SIZE);

            if(StringUtils.isBlank(tableName)){
            	ErrorRecord.addError(String.format("配置项有问题，请检查[%s].", TABLE_NAME_PATH));
                throw DataXException.asDataXException( CommonErrorCode.CONFIG_ERROR, String.format("配置项有问题，请检查[%s].", TABLE_NAME_PATH));
            }

            if(masterAddresses == null || masterAddresses.size() == 0){
            	ErrorRecord.addError(String.format("配置项有问题，请检查[%s].", MASTER_ADDRESSES_PATH));
                throw DataXException.asDataXException( CommonErrorCode.CONFIG_ERROR, String.format("配置项有问题，请检查[%s].", MASTER_ADDRESSES_PATH));
            }

            if(columnList == null || columnList.size() == 0){
            	ErrorRecord.addError(String.format("配置项有问题，请检查[%s].", COLUMNS_PATH));
                throw DataXException.asDataXException( CommonErrorCode.CONFIG_ERROR, String.format("配置项有问题，请检查[%s].", COLUMNS_PATH));
            }

            if(recodeSizeCommitBuffer == null || recodeSizeCommitBuffer < 1){
            	ErrorRecord.addError(String.format("配置项有问题，请检查[%s].", RECORD_BUFFER_SIZE));
                throw DataXException.asDataXException( CommonErrorCode.CONFIG_ERROR, String.format("配置项有问题，请检查[%s].", RECORD_BUFFER_SIZE));
            }
            try {
                KuduUtils.getMetaTable(masterAddresses, tableName, columnList.toArray(new String[columnList.size()]));
                LOG.info("kuduWrite job verity successful!");
            } catch (Exception e) {
            	ErrorRecord.addError(e);
                throw DataXException.asDataXException(CONFIG_INVALID_EXCEPTION,e.getMessage());
            }
        }


    }


    public static class Task extends Writer.Task {
        private Configuration configuration;
        private KuduClient client;
        private KuduTable kuduTable;
        private KuduSession session;
        private MetaTable metaTable;
        private List<String> configColumnNames;
        private int recodeSizeCommitBuffer;

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            LOG.info("begin do write...");
            Record record = null;
            try {
                int recordBufferSize = 0;
                List<PartialRow> printRows = new ArrayList<PartialRow>();
                while ((record = lineReceiver.getFromReader()) != null) {
                    Upsert upsert = kuduTable.newUpsert();
                    PartialRow row = upsert.getRow();
                    KuduRowUtils.RecordWrite(record, row, metaTable, this.configColumnNames);
                    this.session.apply(upsert);
                    printRows.add(row);
                    recordBufferSize++;
                    if(recordBufferSize > recodeSizeCommitBuffer/2){
                        List<OperationResponse> responses = session.flush();
                        checkError(responses,printRows);
                        recordBufferSize = 0;
                    }
                }

                List<OperationResponse> responses = session.flush();
                checkError(responses,printRows);
            } catch (Exception e) {
                if(record != null){
                    LOG.error("writer error by data:" + record.toString());
                }
                LOG.error("kudu startWrite error", e);
                throw new RuntimeException(e);
            }
            LOG.info("end do write");
        }


        private void checkError(List<OperationResponse> responses, List<PartialRow> printRecords){
            for(OperationResponse response : responses){
                boolean error = response.hasRowError();
                if(error){
                    LOG.error("write kudu data:" +printRecords.toString());
                    printRecords.clear();
                    throw DataXException.asDataXException(KuduWriterErrorCode.COLUMN_WRITER_ERROR,response.getRowError().toString());
                }
                printRecords.clear();
            }
        }


        @Override
        public void init() {
            this.configuration = super.getPluginJobConf();
            String tableName = String.valueOf(configuration.get(TABLE_NAME_PATH));
            List<String> masterAddresses = this.configuration.getList(MASTER_ADDRESSES_PATH, String.class);
            this.recodeSizeCommitBuffer = this.configuration.getInt(RECORD_BUFFER_SIZE);
            this.recodeSizeCommitBuffer = recodeSizeCommitBuffer < 1 ? 1000 : recodeSizeCommitBuffer;
            this.configColumnNames = (List<String>) configuration.get(COLUMNS_PATH);

            try {
                this.metaTable = KuduUtils.getMetaTable(masterAddresses, tableName, configColumnNames.toArray(new String[configColumnNames.size()]));
            } catch (Exception e) {
                throw DataXException.asDataXException(KuduWriterErrorCode.Write_FILE_IO_ERROR,e.getMessage());
            }
            try {
                this.client = KuduUtils.createClient(masterAddresses);
                this.kuduTable = this.client.openTable(tableName);
                this.session = client.newSession();
                session.setFlushMode(SessionConfiguration.FlushMode.MANUAL_FLUSH);
                session.setMutationBufferSpace(recodeSizeCommitBuffer);
            } catch (KuduException e) {
               throw new DataXException(KuduWriterErrorCode.CONNECT_HDFS_IO_ERROR,e.getMessage());
            }
        }

        @Override
        public void destroy() {
            if (this.session != null) {
                try {
                    session.flush();
                    session.close();
                } catch (KuduException e) {
                }
            }

        }
    }


}
