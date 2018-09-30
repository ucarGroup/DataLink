package com.ucar.datalink.reader.hbase.replicate;

import com.ucar.datalink.contract.log.hbase.HColumn;
import com.ucar.datalink.contract.log.hbase.HRecord;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by lubiao on 2017/11/16.
 */
public class HRecordGenerator {

    public static HRecord generate(final TableName tableName, final List<Cell> cells) {

        final byte[] rowkey = CellUtil.cloneRow(cells.get(0));
        final List<HColumn> columns = cells.stream().map(cell -> {
            byte[] family = CellUtil.cloneFamily(cell);
            byte[] qualifier = CellUtil.cloneQualifier(cell);
            byte[] value = CellUtil.cloneValue(cell);
            byte type = cell.getTypeByte();
            long timestamp = cell.getTimestamp();

            return new HColumn(family, qualifier, value, type, timestamp);
        }).collect(toList());

        return new HRecord(tableName.getNamespaceAsString(), tableName.getNameAsString(), rowkey, columns);
    }
}
