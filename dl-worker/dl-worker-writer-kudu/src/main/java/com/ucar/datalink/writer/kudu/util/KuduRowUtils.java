package com.ucar.datalink.writer.kudu.util;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Type;
import org.apache.kudu.client.PartialRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;


public class KuduRowUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KuduRowUtils.class);

    public static void setValue(PartialRow partialRow, Object value, ColumnSchema columnSchema) throws Exception {
        Object writeValue;
        String columnName = columnSchema.getName();
        Type type = columnSchema.getType();

        if (type == Type.STRING) {
            writeValue = value == null ? "" : value;
            partialRow.addString(columnName, writeValue.toString());
            return;
        } else if (type == Type.BINARY) {
            writeValue = value == null ? "" : value;
            partialRow.addBinary(columnName, writeValue.toString().getBytes());
            return;
        }

        writeValue = (value == null || value.toString().trim().equals("") || value.toString().equalsIgnoreCase("null")) ? 0 : value;
        try {
            if (type == Type.INT16) {
                partialRow.addShort(columnName, Short.parseShort(writeValue.toString()));
                return;
            } else if (type == Type.INT8) {
                partialRow.addByte(columnName, Byte.parseByte(writeValue.toString()));
                return;
            } else if (type == Type.INT32) {
                partialRow.addInt(columnName, Integer.parseInt(writeValue.toString()));
                return;
            } else if (type == Type.INT64) {
                partialRow.addLong(columnName, Long.parseLong(writeValue.toString()));
                return;
            } else if (type == Type.BOOL) {
                partialRow.addBoolean(columnName, Boolean.parseBoolean(writeValue.toString()));
                return;
            } else if (type == Type.FLOAT) {
                partialRow.addFloat(columnName, Float.parseFloat(writeValue.toString()));
                return;
            } else if (type == Type.DOUBLE) {
                partialRow.addDouble(columnName, Double.parseDouble(writeValue.toString()));
                return;
            }
        } catch (NumberFormatException e) {
            String msg = String.format("column[%s] value[%s] convert type[%s]  errror!", columnName, value, type.toString());
            LOG.error(msg, e);
            throw new DatalinkException(msg, e);
        }
        if (type == Type.DECIMAL) {
            int scale = columnSchema.getTypeAttributes().getScale();
            BigDecimal formatValue = null;
            try {
                formatValue = new BigDecimal(String.valueOf(writeValue)).setScale(scale, BigDecimal.ROUND_HALF_UP);
                partialRow.addDecimal(columnName, formatValue);
                return;
            } catch (Exception e) {
                String msg = String.format("column[%s] value[%s] convert type[%s]  value[%s] errror!", columnName, writeValue, type.toString(), formatValue == null ? "" : formatValue.toString());
                LOG.error(msg, e);
                throw new DatalinkException(msg, e);
            }
        }
        throw new Exception(String.format("column[%s] type[%s] not support!", columnName, type.toString()));
    }


}
