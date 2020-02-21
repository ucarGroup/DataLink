package com.ucar.datalink.flinker.plugin.reader.hbasereader98.util;

import com.ucar.datalink.flinker.api.element.*;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.ColumnType;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HbaseReaderErrorCode;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Key;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public abstract class MultiVersionTask extends HbaseAbstractTask {
    private static byte[] COLON_BYTE;

    private int maxVersion;
    private List<KeyValue> kvList = new ArrayList<KeyValue>();
    private int currentReadPosition = 0;

    // 四元组的类型
    private ColumnType rowkeyReadoutType = null;
    private ColumnType columnReadoutType = null;
    private ColumnType timestampReadoutType = null;
    private ColumnType valueReadoutType = null;

    public MultiVersionTask(Configuration configuration) {
        super(configuration);

        this.maxVersion = configuration.getInt(Key.MAX_VERSION);
        List<String> userConfiguredTetradTypes = configuration.getList(Key.TETRAD_TYPE, String.class);

        this.rowkeyReadoutType = ColumnType.getByTypeName(userConfiguredTetradTypes.get(0));
        this.columnReadoutType = ColumnType.getByTypeName(userConfiguredTetradTypes.get(1));
        this.timestampReadoutType = ColumnType.getByTypeName(userConfiguredTetradTypes.get(2));
        this.valueReadoutType = ColumnType.getByTypeName(userConfiguredTetradTypes.get(3));

        try {
            MultiVersionTask.COLON_BYTE = ":".getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
            throw DataXException.asDataXException(HbaseReaderErrorCode.PREPAR_READ_ERROR, "系统内部获取 列族与列名冒号分隔符的二进制时失败.", e);
        }
    }

    private void convertKVToLine(KeyValue keyValue, Record record) throws Exception {
        byte[] rawRowkey = keyValue.getRow();

        long timestamp = keyValue.getTimestamp();

        byte[] cfAndQualifierName = Bytes.add(keyValue.getFamily(), MultiVersionTask.COLON_BYTE, keyValue.getQualifier());

        record.addColumn(convertBytesToAssignType(this.rowkeyReadoutType, rawRowkey));

        record.addColumn(convertBytesToAssignType(this.columnReadoutType, cfAndQualifierName));

        // 直接忽略了用户配置的 timestamp 的类型
        record.addColumn(new LongColumn(timestamp));

        record.addColumn(convertBytesToAssignType(this.valueReadoutType, keyValue.getValue()));
    }

    private Column convertBytesToAssignType(ColumnType columnType, byte[] byteArray) throws UnsupportedEncodingException {
        Column column;
        switch (columnType) {
            case BOOLEAN:
                column = new BoolColumn(byteArray == null ? null : Bytes.toBoolean(byteArray));
                break;
            case SHORT:
                column = new LongColumn(byteArray == null ? null : String.valueOf(Bytes.toShort(byteArray)));
                break;
            case INT:
                column = new LongColumn(byteArray == null ? null : Bytes.toInt(byteArray));
                break;
            case LONG:
                column = new LongColumn(byteArray == null ? null : Bytes.toLong(byteArray));
                break;
            case BYTES:
                column = new BytesColumn(byteArray);
                break;
            case FLOAT:
                column = new DoubleColumn(byteArray == null ? null : Bytes.toFloat(byteArray));
                break;
            case DOUBLE:
                column = new DoubleColumn(byteArray == null ? null : Bytes.toDouble(byteArray));
                break;
            case STRING:
                column = new StringColumn(byteArray == null ? null : new String(byteArray, super.encoding));
                break;
            case BINARY_STRING:
                column = new StringColumn(byteArray == null ? null : Bytes.toStringBinary(byteArray));
                break;

            default:
                throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE, "Hbasereader 不支持您配置的列类型:" + columnType);
        }

        return column;
    }

    @Override
    public boolean fetchLine(Record record) throws Exception {
        Result result;
        if (this.kvList.size() == this.currentReadPosition) {
            result = super.getNextHbaseRow();
            if (result == null) {
                return false;
            }

            this.kvList = result.list();
            if (this.kvList == null) {
                return false;
            }

            this.currentReadPosition = 0;
        }

        try {
            KeyValue keyValue = this.kvList.get(this.currentReadPosition);
            convertKVToLine(keyValue, record);
        } catch (Exception e) {
            throw e;
        } finally {
            this.currentReadPosition++;
        }

        return true;
    }

    public void setMaxVersions(Scan scan) {
        if (this.maxVersion == -1 || this.maxVersion == Integer.MAX_VALUE) {
            scan.setMaxVersions();
        } else {
            scan.setMaxVersions(this.maxVersion);
        }
    }

}
