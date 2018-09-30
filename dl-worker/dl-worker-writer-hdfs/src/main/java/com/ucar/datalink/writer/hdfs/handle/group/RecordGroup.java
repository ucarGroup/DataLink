package com.ucar.datalink.writer.hdfs.handle.group;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lubiao on 2017/12/15.
 */
public class RecordGroup<T extends Record> {

    private List<GroupData<T>> groupDatas = new ArrayList<>();

    public RecordGroup(List<T> records, TaskWriterContext context) {
        records.stream().forEach(r -> {
            GroupData<T> groupData = findGroupData(r);
            groupData.getRecords().add(r);
        });
    }

    private GroupData<T> findGroupData(T record) {
        GroupKey key = new GroupKey(record.RSI().getNamespace(), record.RSI().getName());

        for (GroupData<T> group : groupDatas) {
            if (group.getGroupKey().equals(key)) {
                return group;
            }
        }

        GroupData<T> groupData = new GroupData<>(key);
        groupDatas.add(groupData);
        return groupData;
    }

    public List<GroupData<T>> getGroupDatas() {
        return groupDatas;
    }
}