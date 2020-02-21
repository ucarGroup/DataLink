package com.ucar.datalink.flinker.plugin.writer.kuduwriter;

import com.ucar.datalink.flinker.api.element.Column;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Type;
import org.apache.kudu.client.PartialRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class KuduRowUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KuduRowUtils.class);


    public static void RecordWrite(Record record, PartialRow partialRow, MetaTable metaTable, List<String> configColumnNames) throws Exception {

        int columnNumber = record.getColumnNumber();
        for (int i = 0; i < columnNumber; i++) {
            ColumnSchema columnSchema = metaTable.getColumnSchema(configColumnNames.get(i));
            Object value = record.getColumn(i).getRawData();
            if(value != null && !String.valueOf(value.toString()).trim().equals("") && record.getColumn(i).getType().equals(Column.Type.DATE)){
            	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                value = simpleDateFormat.format(record.getColumn(i).asDate());
            }

            setValue(partialRow,value ,columnSchema);
        }
    }

    private static void setValue(PartialRow partialRow,Object value,ColumnSchema columnSchema) throws Exception {
        Object writeValue;
        String columnName = columnSchema.getName();
        Type type = columnSchema.getType();

        if (type == Type.STRING){
            writeValue =  value == null ? "" : value;
            partialRow.addString(columnName,writeValue.toString());
            return;
        } else if( type == Type.BINARY){
            writeValue =  value == null ? "" : value;
            partialRow.addBinary(columnName,writeValue.toString().getBytes());
            return;
        }

        writeValue = (value == null || value.toString().trim().equals("") || value.toString().equalsIgnoreCase("null")) ? 0 : value;
        try {
            if (type == Type.INT16){
                partialRow.addShort(columnName,Short.parseShort(writeValue.toString()));
                return;
            } else if (type == Type.INT8){
                partialRow.addByte(columnName,Byte.parseByte(writeValue.toString()));
                return;
            }else if(type == Type.INT32){
                partialRow.addInt(columnName,Integer.parseInt(writeValue.toString()));
                return;
            } else if (type == Type.INT64){
                partialRow.addLong(columnName,Long.parseLong(writeValue.toString()));
                return;
            }else if( type == Type.BOOL){
                partialRow.addBoolean(columnName, Boolean.parseBoolean(writeValue.toString()));
                return;
            } else if( type == Type.FLOAT){
                partialRow.addFloat(columnName, Float.parseFloat(writeValue.toString()));
                return;
            } else if( type == Type.DOUBLE){
                partialRow.addDouble(columnName, Double.parseDouble(writeValue.toString()));
                return;
            }
        } catch (NumberFormatException e) {
            String msg = String.format("column[%s] value[%s] convert type[%s]  errror!",columnName, value,type.toString());
            LOG.error(msg,e);
            ErrorRecord.addError(msg+e.getMessage());
            throw new Exception(msg,e);
        }
        if( type == Type.DECIMAL){
            try {
                partialRow.addDecimal(columnName, new BigDecimal(String.valueOf(writeValue)));
                return;
            } catch (Exception e) {
                String msg = String.format("column[%s] value[%s] convert type[%s]  errror!",columnName, writeValue,type.toString());
                LOG.error(msg,e);
                ErrorRecord.addError(msg+e.getMessage());
                throw new Exception(msg,e);
            }
        }
        ErrorRecord.addError(String.format("column[%s] type[%s] not support!",columnName,type.toString()));
        throw new Exception(String.format("column[%s] type[%s] not support!",columnName,type.toString()));
    }








}
