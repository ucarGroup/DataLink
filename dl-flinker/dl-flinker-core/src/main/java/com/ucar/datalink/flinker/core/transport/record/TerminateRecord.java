package com.ucar.datalink.flinker.core.transport.record;

import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.Record;
/**
 * 作为标示 生产者已经完成生产的标志
 * 
 */
public class TerminateRecord implements Record {
	private final static TerminateRecord SINGLE = new TerminateRecord();

	private Object id;

	private TerminateRecord() {
	}

	public static TerminateRecord get() {
		return SINGLE;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public void addColumn(Column column) {
	}

	@Override
	public Column getColumn(int i) {
		return null;
	}

	@Override
	public int getColumnNumber() {
		return 0;
	}

	@Override
	public int getByteSize() {
		return 0;
	}

	@Override
	public int getMemorySize() {
		return 0;
	}

	@Override
	public void setColumn(int i, Column column) {
		return;
	}
}
