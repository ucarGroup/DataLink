package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.HTable;

import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.spi.Writer;
import com.ucar.datalink.flinker.api.util.Configuration;

public class HbaseWriter98 extends Writer {

	public static class Job extends Writer.Job {
		private Configuration originalConfig = null;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();
		}

		@Override
		public void destroy() {

		}

		@Override
		public List<Configuration> split(int mandatoryNumber) {
			List<Configuration> result = new ArrayList<Configuration>();
			for (int i = 0; i < mandatoryNumber; i++) {
				result.add(originalConfig.clone());
			}
			return result;
		}

	}

	public static class Task extends Writer.Task {

		private Configuration writerSliceConfig;

		@Override
		public void init() {
			this.writerSliceConfig = this.getPluginJobConf();
		}

		@Override
		public void destroy() {

		}

		@Override
		public void startWrite(RecordReceiver lineReceiver) {
			HTable table = HbaseUtil.initHtable(writerSliceConfig);
			HbaseUtil.startWrite(lineReceiver, table, writerSliceConfig);
		}
	}

}
