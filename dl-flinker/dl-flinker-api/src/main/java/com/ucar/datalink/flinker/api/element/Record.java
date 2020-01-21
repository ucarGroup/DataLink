package com.ucar.datalink.flinker.api.element;

/**
 * Created by jingxing on 14-8-24.
 */

public interface Record {

	//id 并不是必须的，按实际情况使用
	public Object getId();

	public void setId(Object id);

	public void addColumn(Column column);

	public void setColumn(int i, final Column column);

	public Column getColumn(int i);

	public String toString();

	public int getColumnNumber();

	public int getByteSize();

	public int getMemorySize();

}
