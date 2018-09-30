package com.ucar.datalink.worker.api.intercept;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 拦截特定主键的默认处理器
 * 如果此拦截器拦截到了主键为skipId的record，则说明对应的writer插件直接跳过该record，不进行处理
 *
 * 支持两种过滤类型：
 *      1、指定ID，多个以逗号分割，如：100,200,300
 *      2、指定ID区域，多个以逗号分割，如：[100-200],[300-500]
 *
 * Created by sqq on 2017/8/9.
 */
public class SkipIdsInterceptor implements Interceptor {
    @Override
    public Record intercept(Record record, TaskWriterContext context) {
        MediaMappingInfo mediaMappingInfo = RecordMeta.mediaMapping(record);
        String skipIds = mediaMappingInfo.getSkipIds();
        if (StringUtils.isBlank(skipIds)) {
            return record;
        }
        String[] skipIdsArr = skipIds.split(",");
        List<String> skipIdsList = new ArrayList<>();
        Collections.addAll(skipIdsList, skipIdsArr);
        String[] key = record.getId().toString().split("\\|");
        if (key.length > 1) {
            throw new RuntimeException("不支持联合主键 : " + Arrays.toString(key) + "");
        }
        if (key.length == 1) {
            if (!skipIds.startsWith("[")) {
                for (String skipId : skipIdsList) {
                    if (skipId.equals(key[0])) {
                        return null;
                    }
                }
            } else {
                for (String skipIdArea : skipIdsList) {
                    int begin = skipIdArea.indexOf("[");
                    int middle = skipIdArea.indexOf("-", begin + 1);
                    int end = skipIdArea.indexOf("]", middle + 1);
                    int skipIdBegin = Integer.parseInt(skipIdArea.substring(begin + 1, middle));
                    int skipIdEnd = Integer.parseInt(skipIdArea.substring(middle + 1, end));
                    int keyVal = Integer.parseInt(key[0]);
                    if ((skipIdBegin <= keyVal) && (keyVal <= skipIdEnd)) {
                        return null;
                    }
                }
            }
        }
        return record;
    }
}
