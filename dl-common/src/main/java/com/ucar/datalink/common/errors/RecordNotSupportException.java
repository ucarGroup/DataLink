package com.ucar.datalink.common.errors;

/**
 * When a task can not handle the record,throw this errors.
 * <p>
 * Created by lubiao on 2017/3/3.
 */
public class RecordNotSupportException extends DatalinkException {
    private Class recordClass;

    public RecordNotSupportException(Class recordClass) {
        super(String.format("The record type %s is not support.", recordClass.getCanonicalName()));
        this.recordClass = recordClass;
    }

    public Class getRecordClass() {
        return recordClass;
    }
}
