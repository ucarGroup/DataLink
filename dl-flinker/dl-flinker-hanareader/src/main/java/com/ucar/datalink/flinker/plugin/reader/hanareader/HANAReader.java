package com.ucar.datalink.flinker.plugin.reader.hanareader;

import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.spi.Reader;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.rdbms.reader.CommonRdbmsReader;
import com.ucar.datalink.flinker.plugin.rdbms.reader.Key;
import com.ucar.datalink.flinker.plugin.rdbms.reader.util.HintUtil;
import com.ucar.datalink.flinker.plugin.rdbms.util.DBUtilErrorCode;
import com.ucar.datalink.flinker.plugin.rdbms.util.DataBaseType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class HANAReader extends Reader {

	private static final DataBaseType DATABASE_TYPE = DataBaseType.HANA;

	public static class Job extends Reader.Job {
		private static final Logger LOG = LoggerFactory .getLogger(Job.class);

		private Configuration originalConfig = null;
		private CommonRdbmsReader.Job commonRdbmsReaderJob;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();
			
			dealFetchSize(this.originalConfig);

			this.commonRdbmsReaderJob = new CommonRdbmsReader.Job(
					DATABASE_TYPE);
			this.commonRdbmsReaderJob.init(this.originalConfig);

			// 注意：要在 this.commonRdbmsReaderJob.init(this.originalConfig); 之后执行，这样可以直接快速判断是否是querySql 模式
			dealHint(this.originalConfig);
		}

        @Override
        public void preCheck(){
            init();
            this.commonRdbmsReaderJob.preCheck(this.originalConfig,DATABASE_TYPE);
        }

		@Override
		public List<Configuration> split(int adviceNumber) {
			return this.commonRdbmsReaderJob.split(this.originalConfig,
					adviceNumber);
		}

		@Override
		public void post() {
			this.commonRdbmsReaderJob.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsReaderJob.destroy(this.originalConfig);
		}

		private void dealFetchSize(Configuration originalConfig) {
			int fetchSize = originalConfig.getInt(
					com.ucar.datalink.flinker.plugin.rdbms.reader.Constant.FETCH_SIZE,
					Constant.DEFAULT_FETCH_SIZE);
			if (fetchSize < 1) {
				throw DataXException
						.asDataXException(DBUtilErrorCode.REQUIRED_VALUE,
								String.format("您配置的 fetchSize 有误，fetchSize:[%d] 值不能小于 1.",
										fetchSize));
			}

			originalConfig.set(
					com.ucar.datalink.flinker.plugin.rdbms.reader.Constant.FETCH_SIZE,
					fetchSize);
		}

		private void dealHint(Configuration originalConfig) {
			String hint = originalConfig.getString(Key.HINT);
			if (StringUtils.isNotBlank(hint)) {
				boolean isTableMode = originalConfig.getBool(com.ucar.datalink.flinker.plugin.rdbms.reader.Constant.IS_TABLE_MODE).booleanValue();
				if(!isTableMode){
					throw DataXException.asDataXException(HANAReaderErrorCode.HINT_ERROR, "当且仅当非 querySql 模式读取 oracle 时才能配置 HINT.");
				}
				HintUtil.initHintConf(DATABASE_TYPE, originalConfig);
			}
		}
	}

	public static class Task extends Reader.Task {

		private Configuration readerSliceConfig;
		private CommonRdbmsReader.Task commonRdbmsReaderTask;

		@Override
		public void init() {
			this.readerSliceConfig = super.getPluginJobConf();
			this.commonRdbmsReaderTask = new CommonRdbmsReader.Task(
					DATABASE_TYPE ,super.getTaskGroupId(), super.getTaskId());
			this.commonRdbmsReaderTask.init(this.readerSliceConfig);
		}

		@Override
		public void startRead(RecordSender recordSender) {
			int fetchSize = this.readerSliceConfig
					.getInt(com.ucar.datalink.flinker.plugin.rdbms.reader.Constant.FETCH_SIZE);

			this.commonRdbmsReaderTask.startRead(this.readerSliceConfig,
					recordSender, super.getTaskPluginCollector(), fetchSize);
		}

		@Override
		public void post() {
			this.commonRdbmsReaderTask.post(this.readerSliceConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsReaderTask.destroy(this.readerSliceConfig);
		}

	}

}
