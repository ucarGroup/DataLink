package com.ucar.datalink.writer.sddl.manager.generatesql;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.writer.sddl.exception.SddlSqlException;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL生成器接口
 */
public abstract class SQLGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLGenerator.class);

	
	/**
	 * 生成sql
	 * @return
	 */
	public void generate(RdbEventRecord dataRow, String tableName, SddlExcuteData.PreparedSqlInfo preparedSqlInfo){
		try {
			executeGenerate(dataRow, tableName, preparedSqlInfo);
		}catch (Exception e){
			LOGGER.error("generate sql error, type: "+dataRow.getEventType()
					+",data:" + dataRow.toString(), e);
			throw new SddlSqlException(e);
		}
	}

    /**
     * 生成sql
     * @return
	public String generate(RdbEventRecord dataRow, EventColumn cell){
		try {
			String tableName = dataRow.getDataTable().getMapping().getAlias();
			if(StringUtils.isBlank(tableName)){
				tableName = dataRow.getDataTable().getTableName();//如果别名为空，再取表名
			}
			return executeGenerate(dataRow, tableName, typeEnum);
		}catch (Exception e){
			LOGGER.error("generate sql error, type: "+dataRow.getEventType()
					+",data:" + dataRow.toString(), e);
			throw new SQLGenetateException(e);
		}
	}

	public String generateWithoutId(DataRow dataRow){
		try {
			String tableName = dataRow.getDataTable().getMapping().getAlias();
			if(StringUtils.isBlank(tableName)){
				tableName = dataRow.getDataTable().getTableName();//如果别名为空，再取表名
			}
			return executeGenerateWithoutId(dataRow,tableName);
		}catch (Exception e){
			LOGGER.error("generate sql error, type: "+dataRow.getEventType()
					+",data:" + dataRow.toString(), e);
			throw new SQLGenetateException(e);
		}
	}
	 */


	/**
	 * 执行生成sql
	 * @return
	 */
	public abstract void executeGenerate(RdbEventRecord dataRow, String tableName, SddlExcuteData.PreparedSqlInfo preparedSqlInfo);

	public String executeGenerateWithoutId(RdbEventRecord dataRow, String tableName){
		throw new SddlSqlException("not support this type:{}" + dataRow.getEventType().toString());
	}
}
