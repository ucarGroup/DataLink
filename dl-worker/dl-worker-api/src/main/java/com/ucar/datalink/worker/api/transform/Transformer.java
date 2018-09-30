package com.ucar.datalink.worker.api.transform;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

/**
 * 根据相关的配置信息，将从TaskReader取到的Record进行转换.
 * 此处的转换只是一些较抽象和共用的转换，每个TaskWriter还可以针对自己的特定需求做进一步处理.
 * <p>
 * <p>
 * Created by user on 2017/3/8.
 */
public abstract class Transformer<T extends Record> {

    public Transformer() {

    }

    public RecordChunk<T> transform(RecordChunk<T> recordChunk, TaskWriterContext context) {
        Long taskId = Long.valueOf(context.taskId());
        RecordChunk<T> newChunk = recordChunk.copyWithoutRecords();

        for (T record : recordChunk.getRecords()) {
            T result = transformOne(record, RecordMeta.mediaMapping(record), context);
            if (result != null) {
                newChunk.merge(result);
            }
        }

        return newChunk;
    }

    protected abstract T transformOne(T record, MediaMappingInfo mappingInfo, TaskWriterContext context);
}
