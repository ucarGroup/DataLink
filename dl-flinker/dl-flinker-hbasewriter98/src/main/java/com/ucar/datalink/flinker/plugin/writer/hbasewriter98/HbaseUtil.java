package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordReceiver;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
/**
 * Hbase工具类
 * 
 * @author lubiao
 * 
 */
public class HbaseUtil {
	private static Logger LOG = LoggerFactory.getLogger(HbaseUtil.class);

	public static HTable initHtable(Configuration configuration) {
		String hbaseConnConf = configuration.getString(Key.HBASE_CONFIG);
		String tableName = configuration.getString(Key.TABLE);
		HBaseAdmin admin = null;
		try {
			org.apache.hadoop.conf.Configuration conf = HbaseConf.getHbaseConf(hbaseConnConf);

			HTable htable = new HTable(conf, tableName);
			admin = new HBaseAdmin(conf);
			check(admin, htable);
			htable.setAutoFlushTo(false);

			return htable;
		} catch (Exception e) {
			throw DataXException.asDataXException(HBaseWriter98ErrorCode.INIT_TABLE_ERROR, e);
		} finally {
			if (admin != null) {
				try {
					admin.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}
	}

	public static boolean isRowkeyColumn(String columnName) {
		return Constant.ROWKEY_FLAG.equalsIgnoreCase(columnName);
	}

	@SuppressWarnings("rawtypes")
	public static void startWrite(RecordReceiver lineReceiver, HTable table, Configuration configuration) {
		List<Map> columns = configuration.getList(Key.COLUMN, Map.class);
		Integer batchSize = configuration.getInt(Key.BATCH_SIZE, 100);
		boolean writeToWAL = configuration.getBool(Key.WRITE_TO_WAL, true);

		List<HbaseColumnCell> hbaseColumnCells = parseColumns(columns);

		try {
			Record record = null;
			List<Put> puts = new ArrayList<Put>();
			while ((record = lineReceiver.getFromReader()) != null) {
				puts.add(getPut(hbaseColumnCells, record, writeToWAL));
				if (puts.size() % batchSize == 0) {
					table.put(puts);
					table.flushCommits();
					puts.clear();
				}
			}
			if (!puts.isEmpty()) {
				table.put(puts);
				table.flushCommits();
			}

			table.close();
		} catch (Exception e) {
			String message = String.format("写hbase[%s]时发生IO异常,请检查您的网络是否正常！", table.getName());
			LOG.error(message, e);
			ErrorRecord.addError(message+"->"+e.getMessage());
			throw DataXException.asDataXException(HBaseWriter98ErrorCode.WRITE_HBASE_IO_ERROR, e);
		}
	}

	private static Put getPut(List<HbaseColumnCell> hbaseColumnCells, Record record, boolean writeToWAL) {
		byte[] cf;
		byte[] qualifier;
		HbaseColumnCell cell;

		Put put = new Put(getRowKey(hbaseColumnCells, record));
		if (!writeToWAL) {
			put.setDurability(Durability.SKIP_WAL);
		}

		int size = hbaseColumnCells.size();
		for (int i = 0; i < size;) {
			cell = hbaseColumnCells.get(i);
			if (HbaseUtil.isRowkeyColumn(cell.getColumnName())) {
				i++;
				continue;
			} else {
				cf = cell.getCf();
				qualifier = cell.getQualifier();
				if (cell.isConstant()) {
					put.add(cf, qualifier, cell.getColumnValue().getBytes());
				} else {
					put.add(cf, qualifier, record.getColumn(i).asBytes());
					i++;// 只有非常量的情况才需要++操作
				}
			}
		}

		return put;
	}

	private static byte[] getRowKey(List<HbaseColumnCell> hbaseColumnCells, Record record) {
		int size = hbaseColumnCells.size();
		for (int i = 0; i < size; i++) {
			HbaseColumnCell cell = hbaseColumnCells.get(i);
			if (HbaseUtil.isRowkeyColumn(cell.getColumnName())) {
				return record.getColumn(i).asBytes();
			}
		}
		return null;
	}

	public static List<HbaseColumnCell> parseColumns(List<Map> column) {
		List<HbaseColumnCell> hbaseColumnCells = new ArrayList<HbaseColumnCell>();

		HbaseColumnCell oneColumnCell;

		for (Map<String, String> aColumn : column) {
			String columnName = aColumn.get("name");
			String columnValue = aColumn.get("value");

			oneColumnCell = new HbaseColumnCell.Builder().columnName(columnName).columnValue(columnValue).build();
			hbaseColumnCells.add(oneColumnCell);
		}

		return hbaseColumnCells;
	}

	private static void check(HBaseAdmin admin, HTable htable) throws DataXException, IOException {
		if (!admin.isMasterRunning()) {
			throw new IllegalStateException("HBase master 没有运行, 请检查您的配置 或者 联系 Hbase 管理员.");
		}
		if (!admin.tableExists(htable.getTableName())) {
			throw new IllegalStateException("HBase源头表" + Bytes.toString(htable.getTableName()) + "不存在, 请检查您的配置 或者 联系 Hbase 管理员.");
		}
		if (!admin.isTableAvailable(htable.getTableName()) || !admin.isTableEnabled(htable.getTableName())) {
			throw new IllegalStateException("HBase源头表" + Bytes.toString(htable.getTableName()) + " 不可用, 请检查您的配置 或者 联系 Hbase 管理员.");
		}
	}
}
