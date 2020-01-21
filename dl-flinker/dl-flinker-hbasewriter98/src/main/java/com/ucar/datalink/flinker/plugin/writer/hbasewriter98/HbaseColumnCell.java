package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import com.ucar.datalink.flinker.api.base.BaseObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 描述 hbasereader 插件中，column 配置中的一个单元项实体
 */
public class HbaseColumnCell extends BaseObject {

	// columnName 格式为：列族:列名
	private String columnName;

	private byte[] cf;
	private byte[] qualifier;

	// 对于常量类型，其常量值放到 columnValue 里
	private String columnValue;

	// 当配置了 columnValue 时，isConstant=true（这个成员变量是用于方便使用本类的地方判断是否是常量类型字段）
	private boolean isConstant;

	private HbaseColumnCell(Builder builder) {

		// columnName 和 columnValue 不能都为 null
		Validate.isTrue(builder.columnName != null, "Hbasewriter98中，column的列名称为必配置项.");

		if (builder.columnValue != null) {
			this.isConstant = true;
			this.columnValue = builder.columnValue;
		} else {
			this.isConstant = false;
		}

		this.columnName = builder.columnName;
		// 如果 columnName 不是 rowkey，则必须配置为：列族:列名 格式
		if (!HbaseUtil.isRowkeyColumn(this.columnName)) {

			String promptInfo = "Hbasewriter98中， column 的列配置格式应该是：列族:列名. 您配置的列错误：" + this.columnName;
			String[] cfAndQualifier = this.columnName.split(":");
			Validate.isTrue(
					cfAndQualifier != null && cfAndQualifier.length == 2 && StringUtils.isNotBlank(cfAndQualifier[0])
							&& StringUtils.isNotBlank(cfAndQualifier[1]), promptInfo);

			this.cf = Bytes.toBytes(cfAndQualifier[0].trim());
			if (Constant.QUALIFIER_NULL_FLAG.equals(cfAndQualifier[1].trim())) {
				this.qualifier = null;
			} else {
				this.qualifier = Bytes.toBytes(cfAndQualifier[1].trim());
			}
		}
	}

	public String getColumnName() {
		return columnName;
	}

	public byte[] getCf() {
		return cf;
	}

	public byte[] getQualifier() {
		return qualifier;
	}

	public String getColumnValue() {
		return columnValue;
	}

	public boolean isConstant() {
		return isConstant;
	}

	// 内部 builder 类
	public static class Builder {
		private String columnName;
		private String columnValue;

		public Builder columnName(String columnName) {
			this.columnName = columnName;
			return this;
		}

		public Builder columnValue(String columnValue) {
			this.columnValue = columnValue;
			return this;
		}

		public HbaseColumnCell build() {
			return new HbaseColumnCell(this);
		}
	}
}
