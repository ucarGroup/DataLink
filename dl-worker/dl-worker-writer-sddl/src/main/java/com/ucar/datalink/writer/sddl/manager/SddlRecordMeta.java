package com.ucar.datalink.writer.sddl.manager;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.writer.sddl.model.SddlExcuteData;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Date   : 5:18 PM 15/11/2017
 */
public abstract class SddlRecordMeta {

    // record.metaData()<List<SddlExcuteData>>
    private static final String SddlDs       = "SddlDs";
    private static final String ExcuteSddlDs = "ExcuteSddlDs";

    public static void appendSddlDs(Record record, List<SddlExcuteData> sddlExcuteDatas) {
        if (CollectionUtils.isEmpty(sddlExcuteDatas))
            return;

        List<SddlExcuteData> sddlExcuteDataList = getSddlDs(record);
        if (sddlExcuteDataList == null) {
            sddlExcuteDataList = new ArrayList<>();
            sddlExcuteDataList.addAll(sddlExcuteDatas);

            record.metaData().put(SddlDs, sddlExcuteDataList);
        } else {
            sddlExcuteDataList.addAll(sddlExcuteDatas);
        }
    }

    public static List<SddlExcuteData> getSddlDs(Record record) {
        return (List<SddlExcuteData>) record.metaData().get(SddlDs);
    }

    public static void removeSddlDs(Record record) {
        record.metaData().remove(SddlDs);
    }

    //---------------------excute Datasource----------------------

    public static void attachExcuteSddlDs(Record record, SddlExcuteData sddlExcuteData) {

        record.metaData().put(ExcuteSddlDs, sddlExcuteData);
    }

    public static SddlExcuteData getExcuteSddlDs(Record record) {
        return (SddlExcuteData) record.metaData().get(ExcuteSddlDs);
    }
}
