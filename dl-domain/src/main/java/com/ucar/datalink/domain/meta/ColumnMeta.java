package com.ucar.datalink.domain.meta;

/**
 * Created by user on 2017/6/22.
 */
public class ColumnMeta {

    /**
     * 列名称，关系型数据库就是列的名称，如果是HBase这个字段对应的是quailifer
     */
    private String name;

    /**
     * 列类型
     */
    private String type;

    /**
     * 列长度
     */
    private Integer length;

    /**
     * HBase专用的，用来存储列簇的名称
     */
    private String columnFamily;

    /**
     * 列描述信息
     */
    private String columnDesc;

    /**
     * 浮点数精度(小数点后的长度)
     */
    private Integer decimalDigits;

    /**
     * 专门给关系型数据库使用的，用来标示当前的这个列是否是主键
     */
    private boolean isPrimaryKey;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if( !tryParseDecimal(type) ) {
            this.type = type;
        }
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getColumnDesc() {
        return columnDesc;
    }

    public void setColumnDesc(String columnDesc) {
        this.columnDesc = columnDesc;
    }

    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    @Override
    public String toString() {
        return "ColumnMeta{" +
                "length=" + length +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }


    private boolean tryParseDecimal(String name) {
        try {
            if(name.startsWith("decimal")) {
                String decimalDigits = name.substring(8, name.length() - 1);
                String[] arr = decimalDigits.split(",");
                this.setType("decimal");
                this.setLength(Integer.parseInt(arr[0]));
                this.setDecimalDigits(Integer.parseInt(arr[1]));
                return true;
            }
        } catch(Exception e) {
            //ignore
        }
        return false;
    }

}
