package com.ucar.datalink.flinker.plugin.writer.hbasewriter98;

import com.ucar.datalink.flinker.api.spi.ErrorCode;

public enum HBaseWriter98ErrorCode implements ErrorCode {
	REQUIRED_VALUE("HbaseWriter98-00", "您缺失了必须填写的参数值."), 
	INIT_TABLE_ERROR("HbaseWriter98-01", "初始化 Hbase 表时出错."),
	ILLEGAL_VALUE("HbaseWriter98-02", "您配置的值不合法."),
	WRITE_HBASE_IO_ERROR("HbaseWriter98-03","写hbase是出现异常");

	private final String code;
	private final String description;

	private HBaseWriter98ErrorCode(String code, String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return String.format("Code:[%s], Description:[%s]. ", this.code, this.description);
	}
}
