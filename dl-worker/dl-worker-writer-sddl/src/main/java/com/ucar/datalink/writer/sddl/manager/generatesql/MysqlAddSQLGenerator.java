package com.ucar.datalink.writer.sddl.manager.generatesql;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MysqlAddSQLGenerator extends SQLGenerator {

	private volatile static MysqlAddSQLGenerator instance;

	public static MysqlAddSQLGenerator getInstance () {
		if (instance == null) {
			synchronized (MysqlAddSQLGenerator.class) {
				if (instance == null) {
					instance = new MysqlAddSQLGenerator();
				}
			}
		}

		return instance;
	}

	@Override
	public void executeGenerate(RdbEventRecord dataRow, String tableName, SddlExcuteData.PreparedSqlInfo preparedSqlInfo) {
		List<EventColumn> keyList  = dataRow.getKeys();
		List<EventColumn> cellList = dataRow.getColumns();

		StringBuffer params = new StringBuffer();
		StringBuffer values = new StringBuffer();

		int keySize  = keyList.size();
		int cellSize = cellList.size();
		List<EventColumn> preparedColumns = new ArrayList<>(keySize+cellSize);

		// key
		for (int i = 0; i < keySize; i++) {
			EventColumn cell = keyList.get(i);

			generatorSql(cell, params, values);
			preparedColumns.add(cell);
		}

		// column
		for (int i = 0; i < cellSize; i++) {
			EventColumn cell = cellList.get(i);

			generatorSql(cell, params, values);
			preparedColumns.add(cell);
		}


		String sql = SQLTemplateConstant.getMysqlInsertSql("`" + tableName + "`",
							StringUtils.substringBeforeLast(params.toString(), ",").intern(),
				StringUtils.substringBeforeLast(values.toString(), ","));

		preparedSqlInfo.setSql(sql);
		preparedSqlInfo.setPreparedColumns(preparedColumns);

	}

	private void generatorSql (EventColumn cell, StringBuffer params, StringBuffer values) {
		String cellName = cell.getColumnName();

		params.append("`").append(cellName).append("`,");
		values.append("?,");
	}

}
