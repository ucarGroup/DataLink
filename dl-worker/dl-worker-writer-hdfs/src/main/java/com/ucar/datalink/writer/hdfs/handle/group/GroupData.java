package com.ucar.datalink.writer.hdfs.handle.group;

import com.ucar.datalink.contract.Record;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2017/12/15.
 */
public class GroupData<T extends Record> {

    private GroupKey groupKey;
    private List<T> records;

    public GroupData(GroupKey groupKey) {
        this.groupKey = groupKey;
        this.records = new LinkedList<>();
    }

    public GroupKey getGroupKey() {
        return groupKey;
    }

    public List<T> getRecords() {
        return records;
    }
}
