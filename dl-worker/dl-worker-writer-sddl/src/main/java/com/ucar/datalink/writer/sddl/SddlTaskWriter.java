package com.ucar.datalink.writer.sddl;

import com.google.common.collect.Sets;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.sddl.SddlWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.sddl.handler.RdbEventRecordHandler;
import com.ucar.datalink.writer.sddl.handler.special.All2PartHandler;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 27/10/2017.
 */
public class SddlTaskWriter extends TaskWriter<SddlWriterParameter> {

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        TaskInfo taskInfo = context.getService(TaskConfigService.class).getTask(Long.valueOf(context.taskId()));

        //如果是虚拟数据源，取出真实数据源id
        Long readerMediaSourceId = taskInfo.getReaderMediaSourceId();
        MediaSourceInfo mediaSourceInfo = context.getService(MediaSourceService.class).getById(readerMediaSourceId);
        if(mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
            MediaSourceInfo realMediaSourceInfo = context.getService(MediaService.class).getRealDataSource(mediaSourceInfo);
            readerMediaSourceId = realMediaSourceInfo.getId();
        }
        MediaSourceInfo sddlMediaSourceInfo = tryFindSddlMediaSource(context, readerMediaSourceId);

        if (sddlMediaSourceInfo == null) {
            //如果该Task的读库不是sddl数据源的某个分库，说明不是sdll内部同步，而是合库到分库的同步
            return new All2PartHandler();
        } else {
            return new RdbEventRecordHandler();
        }
    }

    private MediaSourceInfo tryFindSddlMediaSource(TaskWriterContext context, Long readerMediaSourceId) {
        List<MediaSourceInfo> sddlMediaSources = context.getService(MediaSourceService.class).getListByType(Sets.newHashSet(MediaSourceType.SDDL));

        if (CollectionUtils.isEmpty(sddlMediaSources))
            return null;

        List<MediaSourceInfo> resultInfos = new ArrayList<>();
        for (MediaSourceInfo mediaSource : sddlMediaSources) {
            SddlMediaSrcParameter parameter = mediaSource.getParameterObj();

            if (parameter.getPrimaryDbsId().contains(readerMediaSourceId))
                resultInfos.add(mediaSource);

        }

        if (resultInfos.size() == 0) {
            return null;
        } else {
            return resultInfos.get(0);
        }
    }
}
