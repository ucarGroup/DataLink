package com.ucar.datalink.writer.sddl.manager.generatesql;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MysqlUpdateSQLGenerator extends SQLGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(MysqlUpdateSQLGenerator.class);

	private volatile static MysqlUpdateSQLGenerator instance;

	public static MysqlUpdateSQLGenerator getInstance () {
		if (instance == null) {
			synchronized (MysqlUpdateSQLGenerator.class) {
				if (instance == null) {
					instance = new MysqlUpdateSQLGenerator();
				}
			}
		}

		return instance;
	}

	@Override
	public void executeGenerate(RdbEventRecord dataRow, String tableName, SddlExcuteData.PreparedSqlInfo preparedSqlInfo) {
		boolean existOldKeys = !org.springframework.util.CollectionUtils.isEmpty(dataRow.getOldKeys());
		List<EventColumn> keyList  = null;
		List<EventColumn> cellList = null;
		if (existOldKeys) {
			// 需要考虑主键变更的场景
			// 构造sql如下：update table xxx set pk = newPK where pk = oldPk
			keyList  = dataRow.getOldKeys();
			cellList = dataRow.getUpdatedColumns();
			cellList.addAll(dataRow.getKeys());
		} else {
			keyList  = dataRow.getKeys();
			cellList = dataRow.getUpdatedColumns();
		}

		StringBuffer sqlPrefix = new StringBuffer();
		StringBuffer sqlSuffix = new StringBuffer();

		int keySize  = keyList.size();
		int cellSize = cellList.size();
		List<EventColumn> preparedColumns = new ArrayList<>(keySize+cellSize);

		for (int i = 0; i < cellSize; i++) {
			EventColumn cell = cellList.get(i);

			sqlPrefix.append("`").append(cell.getColumnName()).append("`")
					 .append("=?,");
			preparedColumns.add(cell);
		}

		for (int i = 0; i < keySize; i++) {
			EventColumn cell = keyList.get(i);

			sqlSuffix.append(" `").append(cell.getColumnName())
					 .append("`=").append("?").append(" ")
					 .append("AND");
			preparedColumns.add(cell);
		}

		String sql = SQLTemplateConstant.getMysqlUpdateSql("`"+tableName+"`", sqlPrefix.substring(0, sqlPrefix.length() - 1),
					sqlSuffix.substring(0, sqlSuffix.length() - 3));

		preparedSqlInfo.setSql(sql);
		preparedSqlInfo.setPreparedColumns(preparedColumns);
	}

}
