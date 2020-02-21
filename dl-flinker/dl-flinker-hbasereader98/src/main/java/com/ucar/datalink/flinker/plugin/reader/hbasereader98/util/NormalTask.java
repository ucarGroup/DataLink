package com.ucar.datalink.flinker.plugin.reader.hbasereader98.util;

import com.ucar.datalink.flinker.api.element.*;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.ColumnType;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HbaseColumnCell;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.HbaseReaderErrorCode;
import com.ucar.datalink.flinker.plugin.reader.hbasereader98.Key;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.List;
import java.util.Map;

public class NormalTask extends HbaseAbstractTask {
    private List<Map> column;
    private List<HbaseColumnCell> hbaseColumnCells;

    public NormalTask(Configuration configuration) {
        super(configuration);

        this.column = configuration.getList(Key.COLUMN, Map.class);
        this.hbaseColumnCells = HbaseUtil.parseColumnOfNormalMode(this.column);
    }

    @Override
    public boolean fetchLine(Record record) throws Exception {
        Result result = super.getNextHbaseRow();

        if (null == result) {
            return false;
        }
        super.lastResult = result;

        try {
            byte[] hbaseColumnValue;
            String columnName;
            ColumnType columnType;

            byte[] cf;
            byte[] qualifier;

            for (HbaseColumnCell cell : this.hbaseColumnCells) {
                columnType = cell.getColumnType();
                if (cell.isConstant()) {
                    // 对常量字段的处理
                    fillRecordWithConstantValue(record, cell);
                } else {
                    // 根据列名称获取值
                    columnName = cell.getColumnName();

                    if (HbaseUtil.isRowkeyColumn(columnName)) {
                        hbaseColumnValue = result.getRow();
                    } else {
                        cf = cell.getCf();
                        qualifier = cell.getQualifier();
                        hbaseColumnValue = result.getValue(cf, qualifier);
                    }

                    doFillRecord(hbaseColumnValue, columnType, super.encoding, cell.getDateformat(), record);
                }
            }
        } catch (Exception e) {
            // 注意，这里catch的异常，期望是byte数组转换失败的情况。而实际上，string的byte数组，转成整数类型是不容易报错的。但是转成double类型容易报错。

            record.setColumn(0, new StringColumn(Bytes.toStringBinary(result.getRow())));

            throw e;
        }

        return true;
    }

    @Override
    public void initScan(Scan scan) {
        boolean isConstant;
        boolean isRowkeyColumn;
        for (HbaseColumnCell cell : this.hbaseColumnCells) {
            isConstant = cell.isConstant();
            isRowkeyColumn = HbaseUtil.isRowkeyColumn(cell.getColumnName());

            if (!isConstant && !isRowkeyColumn) {
                this.scan.addColumn(cell.getCf(), cell.getQualifier());
            }
        }
    }


    protected void doFillRecord(byte[] byteArray, ColumnType columnType, String encoding, String dateformat, Record record) throws Exception {
        switch (columnType) {
            case BOOLEAN:
                record.addColumn(new BoolColumn(byteArray == null ? null : Bytes.toBoolean(byteArray)));
                break;
            case SHORT:
                record.addColumn(new LongColumn(byteArray == null ? null : String.valueOf(Bytes.toShort(byteArray))));
                break;
            case INT:
                record.addColumn(new LongColumn(byteArray == null ? null : Bytes.toInt(byteArray)));
                break;
            case LONG:
                record.addColumn(new LongColumn(byteArray == null ? null : Bytes.toLong(byteArray)));
                break;
            case BYTES:
                record.addColumn(new BytesColumn(byteArray == null ? null : byteArray));
                break;
            case FLOAT:
                record.addColumn(new DoubleColumn(byteArray == null ? null : Bytes.toFloat(byteArray)));
                break;
            case DOUBLE:
                record.addColumn(new DoubleColumn(byteArray == null ? null : Bytes.toDouble(byteArray)));
                break;
            case STRING:
                record.addColumn(new StringColumn(byteArray == null ? null : new String(byteArray, encoding)));
                break;
            case BINARY_STRING:
                record.addColumn(new StringColumn(byteArray == null ? null : Bytes.toStringBinary(byteArray)));
                break;
            case DATE:
                String dateValue = Bytes.toStringBinary(byteArray);
                record.addColumn(byteArray == null ? null : new DateColumn(DateUtils.parseDate(dateValue, new String[]{dateformat})));
                break;
            default:
                throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE, "Hbasereader 不支持您配置的列类型:" + columnType);
        }
    }

    // 注意：常量列，不支持 binaryString 类型
    private void fillRecordWithConstantValue(Record record, HbaseColumnCell cell) throws Exception {
        String constantValue = cell.getColumnValue();
        ColumnType columnType = cell.getColumnType();
        switch (columnType) {
            case BOOLEAN:
                record.addColumn(new BoolColumn(constantValue));
                break;
            case SHORT:
            case INT:
            case LONG:
                record.addColumn(new LongColumn(constantValue));
                break;
            case BYTES:
                record.addColumn(new BytesColumn(constantValue.getBytes("utf-8")));
                break;
            case FLOAT:
            case DOUBLE:
                record.addColumn(new DoubleColumn(constantValue));
                break;
            case STRING:
                record.addColumn(new StringColumn(constantValue));
                break;
            case DATE:
                record.addColumn(new DateColumn(DateUtils.parseDate(constantValue, new String[]{cell.getDateformat()})));
                break;
            default:
                throw DataXException.asDataXException(HbaseReaderErrorCode.ILLEGAL_VALUE, "Hbasereader 常量列不支持您配置的列类型:" + columnType);
        }
    }
}
