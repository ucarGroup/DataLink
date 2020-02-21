package com.ucar.datalink.flinker.plugin.writer.sqlserverwriter;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.spi.Writer;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.rdbms.util.DBUtilErrorCode;
import com.ucar.datalink.flinker.plugin.rdbms.util.DataBaseType;
import com.ucar.datalink.flinker.plugin.rdbms.writer.CommonRdbmsWriter;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Constant;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SqlServerWriter extends Writer {
    private static final DataBaseType DATABASE_TYPE = DataBaseType.SQLServer;

    public static class Job extends Writer.Job {
        private Configuration originalConfig = null;
        private CommonRdbmsWriter.Job commonRdbmsWriterJob;

        @Override
        public void init() {
            this.originalConfig = super.getPluginJobConf();
            String writeMode = this.originalConfig.getString(Key.WRITE_MODE);
            this.commonRdbmsWriterJob = new CommonRdbmsWriter.Job(DATABASE_TYPE);
            this.commonRdbmsWriterJob.init(this.originalConfig);
            if(writeMode != null) {
                processReplaceMode(this.originalConfig);
            }
        }

        /**
         *
         IF ((SELECT count(1) FROM news2 WHERE news_id=9) > 0 )
             BEGIN
                 UPDATE news2
                 SET news_title='zzz',news_author='zzz',news_summary='wokao~'
                 WHERE news_id=9
             END
         ELSE
             BEGIN
                 INSERT INTO news2(news_id,news_title,news_author,news_summary) values(9,'zzz','zzz','wokao~')
             END

         * @param config
         */
        public void processReplaceMode(Configuration config) {
            String writeMode = this.originalConfig.getString(Key.WRITE_MODE);
            if( writeMode!=null && !writeMode.trim().toLowerCase().startsWith("replace") ) {
                return;
            }
            List<String> columns = originalConfig.getList(Key.COLUMN, String.class);
            String primaryKey = originalConfig.getString(Key.PRIMARY_KEY);
            if(primaryKey==null || "".equals(primaryKey)) {
                throw new RuntimeException("sqlserver write json config format error,primarykey is empty");
            }
            List<String> copy = new CopyOnWriteArrayList<String>(columns);
            for(String s : copy) {
                if(s.equals(primaryKey)) {
                    copy.remove(s);
                }
            }

            StringBuilder sql = new StringBuilder();
            sql.append("IF ((SELECT count(1) FROM %s WHERE ");
            sql.append(primaryKey);
            sql.append("=?) > 0 ) ");
            sql.append("BEGIN ");
            sql.append(" UPDATE %s ");
            sql.append("  SET ");
            for(int i=0;i<copy.size();i++) {
                if(i == copy.size()-1) {
                    sql.append(copy.get(i)).append("=?");
                } else {
                    sql.append(copy.get(i)).append("=?,");
                }
            }
            sql.append(" WHERE ");
            sql.append(primaryKey).append("=?");
            sql.append(" END ");
            sql.append(" ELSE ");
            sql.append(" BEGIN ");
            sql.append(" INSERT INTO %s(");
            for(int i=0;i<columns.size();i++) {
                if(i == columns.size()-1) {
                    sql.append(columns.get(i)).append(") ");
                } else {
                    sql.append(columns.get(i)).append(",");
                }
            }
            sql.append(" VALUES(");
            for(int i=0;i<columns.size();i++) {
                if(i == columns.size()-1) {
                    sql.append("?").append(") ");
                } else {
                    sql.append("?,");
                }
            }
            sql.append(" END");
            originalConfig.set(Constant.INSERT_OR_REPLACE_TEMPLATE_MARK, sql.toString());
        }


        public String processIdentityInsert(String sql) {
            String identityMode = originalConfig.getString(Key.IDENTITY_INSERT);
            if(identityMode==null || "".equals(identityMode.trim())) {
                return sql;
            }
            if(!"on".equalsIgnoreCase(identityMode) || !"true".equalsIgnoreCase(identityMode)) {
                return sql;
            }
            return null;
        }


        @Override
        public void prepare() {
            this.commonRdbmsWriterJob.prepare(this.originalConfig);
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            return this.commonRdbmsWriterJob.split(this.originalConfig,
                    mandatoryNumber);
        }

        @Override
        public void post() {
            this.commonRdbmsWriterJob.post(this.originalConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsWriterJob.destroy(this.originalConfig);
        }

    }

    public static class Task extends Writer.Task {
        private Configuration writerSliceConfig;
        private CommonRdbmsWriter.Task commonRdbmsWriterTask;

        @Override
        public void init() {
            this.writerSliceConfig = super.getPluginJobConf();
            this.commonRdbmsWriterTask = new SqlServerCommonRdbmsWriterTask(DATABASE_TYPE);
            this.commonRdbmsWriterTask.init(this.writerSliceConfig);
        }

        @Override
        public void prepare() {
            this.commonRdbmsWriterTask.prepare(this.writerSliceConfig);
        }

        public void startWrite(RecordReceiver recordReceiver) {
            this.commonRdbmsWriterTask.startWrite(recordReceiver,
                    this.writerSliceConfig, super.getTaskPluginCollector());
        }

        @Override
        public void post() {
            this.commonRdbmsWriterTask.post(this.writerSliceConfig);
        }

        @Override
        public void destroy() {
            this.commonRdbmsWriterTask.destroy(this.writerSliceConfig);
        }

    }

}
