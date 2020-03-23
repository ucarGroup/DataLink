package com.ucar.datalink.flinker.plugin.reader.hbasereader98;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.spi.Reader;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HbaseReader extends Reader {
    public static class Job extends Reader.Job {
        private static Logger LOG = LoggerFactory.getLogger(Job.class);
        private static boolean IS_DEBUG = LOG.isDebugEnabled();

        private Configuration originalConfig;

        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();

            HbaseUtil.doPretreatment(this.originalConfig);

            if (IS_DEBUG) {
                LOG.debug("After init(), now originalConfig is:\n{}\n", this.originalConfig);
            }
        }

        @Override
        public void prepare() {
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            return HbaseSplitUtil.split(this.originalConfig);
        }


        @Override
        public void post() {

        }

        @Override
        public void destroy() {
        }

    }

    public static class Task extends Reader.Task {
        private Configuration taskConfig;
        private static Logger LOG = LoggerFactory.getLogger(Task.class);
        private HbaseAbstractTask hbaseTaskProxy;

        @Override
        public void init() {
            this.taskConfig = super.getPluginJobConf();

            String mode = this.taskConfig.getString(Key.MODE);
            ModeType modeType = ModeType.getByTypeName(mode);

            switch (modeType) {
                case Normal:
                    this.hbaseTaskProxy = new NormalTask(this.taskConfig);
                    break;
                case MultiVersionFixedColumn:
                    this.hbaseTaskProxy = new MultiVersionFixedColumnTask(this.taskConfig);
                    break;
                case MultiVersionDynamicColumn:
                    this.hbaseTaskProxy = new MultiVersionDynamicColumnTask(this.taskConfig);
                    break;
                default:
                    throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE, "Hbasereader 不支持此类模式:" + modeType);
            }
        }

        @Override
        public void prepare() {
            try {
                this.hbaseTaskProxy.prepare();
            } catch (Exception e) {
                throw DataXException.asDataXException(HbaseReaderErrorCode.PREPAR_READ_ERROR, e);
            }
        }

        @Override
        public void startRead(RecordSender recordSender) {
            Record record = recordSender.createRecord();
            boolean fetchOK;
            while (true) {
                try {
                    fetchOK = this.hbaseTaskProxy.fetchLine(record);
                } catch (Exception e) {
                    LOG.info("Exception", e);
                    super.getTaskPluginCollector().collectDirtyRecord(record, e);
                    record = recordSender.createRecord();
                    continue;
                }
                if (fetchOK) {
                    recordSender.sendToWriter(record);
                    record = recordSender.createRecord();
                } else {
                    break;
                }
            }
            recordSender.flush();
        }

        @Override
        public void post() {
            super.post();
        }

        @Override
        public void destroy() {
            if (this.hbaseTaskProxy != null) {
                try {
                    this.hbaseTaskProxy.close();
                } catch (Exception e) {
                    //
                }
            }
        }


    }
}
