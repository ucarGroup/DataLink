package com.ucar.datalink.writer.sddl.manager.generatesql;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;

import java.util.ArrayList;
import java.util.List;


public class MysqlDeleteSQLGenerator extends SQLGenerator {

	private volatile static MysqlDeleteSQLGenerator instance;

	public static MysqlDeleteSQLGenerator getInstance () {
		if (instance == null) {
			synchronized (MysqlDeleteSQLGenerator.class) {
				if (instance == null) {
					instance = new MysqlDeleteSQLGenerator();
				}
			}
		}

		return instance;
	}

	@Override
	public void executeGenerate(RdbEventRecord dataRow, String tableName, SddlExcuteData.PreparedSqlInfo preparedSqlInfo){
		List<EventColumn> keyList  = dataRow.getKeys();

		StringBuffer whereCond = new StringBuffer();

		int keySize  = keyList.size();
		List<EventColumn> preparedColumns = new ArrayList<>(keySize);

		for (int i = 0; i < keyList.size(); i++) {
			EventColumn cell = keyList.get(i);

			whereCond.append(" `").append(cell.getColumnName())
					 .append("`=?").append(" ")
					 .append("AND");
			preparedColumns.add(cell);
		}

		String sql = SQLTemplateConstant.getMysqlDeleteSql("`"+tableName+"`", whereCond.substring(0, whereCond.length() - 3));
		preparedSqlInfo.setSql(sql);
		preparedSqlInfo.setPreparedColumns(preparedColumns);
	}


}
