package com.ucar.datalink.flinker.plugin.reader.hbasereader98;

import java.util.Arrays;

public class HbaseColumnConfig {
	public String[] columnTypes = null;
	public String[] columnFamilyAndQualifiers = null;

	public HbaseColumnConfig() {
	}

	@Override
	public String toString() {
		if (null != columnTypes && null != columnFamilyAndQualifiers) {
			return "columnTypes:" + Arrays.asList(columnTypes) + "\n"
					+ "columnNames:" + Arrays.toString(columnFamilyAndQualifiers);
		} else {
			return null;
		}
	}
}
