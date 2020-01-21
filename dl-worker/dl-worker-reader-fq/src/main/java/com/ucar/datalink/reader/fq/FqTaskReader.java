package com.ucar.datalink.reader.fq;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.fq.FqChunk;
import com.ucar.datalink.domain.plugin.reader.fq.FqReaderParameter;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskReader;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import com.zuche.flexibleq.client.consumer.ConsumerConfig;
import com.zuche.framework.metaq.manager.ConfigCenterManager;
import com.zuche.framework.metaq.manager.MetaqConsumerManager;
import com.zuche.framework.metaq.vo.ConsumerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by sqq on 2017/6/13.
 */
public class FqTaskReader extends TaskReader<FqReaderParameter, Record> {

    private static final Logger logger = LoggerFactory.getLogger(FqTaskReader.class);
    private ArrayBlockingQueue<FqChunk> queue;
    private FqChunk fqChunk;

    static{
        System.setProperty("sz.framework.projectName", "datalink");
    }

    @Override
    public void start() {
        if (isStart()) {
            return;
        }

        //初始化gaea,fq不关心业务线
        DefaultConfigCenterManager.getInstance();

        //fq要求必须要初始化，且调用多次也没有关系，只生效第一次,参数值也没有关系
        ConfigCenterManager.initDefaultPrefixOnce("ucar");

        FqMessageListener fqMessageListener = new FqMessageListener(queue);
        this.queue = fqMessageListener.queue;
        ConsumerVO consumerVO = new ConsumerVO();
        ConsumerConfig config = new ConsumerConfig();
        MediaSourceInfo fqMediaSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(parameter.getMediaSourceId());
        FqMediaSrcParameter fqParameter = fqMediaSourceInfo.getParameterObj();
        MediaSourceInfo zkMediaSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(fqParameter.getZkMediaSourceId());
        consumerVO.setServers(((ZkMediaSrcParameter) zkMediaSourceInfo.getParameterObj()).getServers());
        consumerVO.setPrefix(fqParameter.getClusterPrefix());
        consumerVO.setTopic(fqParameter.getTopic());
        consumerVO.setMessageListener(fqMessageListener);
        config.setGroup(parameter.getGroup());
        config.setOffset(parameter.getOffset());
        config.setMaxFetchRetries(parameter.getMaxFetchRetries());
        config.setCommitOffsetPeriodInMills(parameter.getCommitOffsetPeriodInMills());
        config.setMaxDelayFetchTimeInMills(parameter.getMaxDelayFetchTimeInMills());
        consumerVO.setConfig(config);
        List<ConsumerVO> consumerVOList = new ArrayList<>();
        consumerVOList.add(consumerVO);
        MetaqConsumerManager.getInstance(consumerVOList, "");

        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void close() {
        MetaqConsumerManager.stopConsumer();
    }

    @Override
    protected RecordChunk<Record> fetch() throws InterruptedException {
        fqChunk = queue.take();
        long firstEntryTime = 0;
        return new RecordChunk<>(fqChunk.getRecordList(), firstEntryTime, 0);
    }

    @Override
    protected void dump(RecordChunk<Record> recordChunk) {

    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void commit(RecordChunk<Record> recordChunk) throws InterruptedException {
        fqChunk.getCallback().onCompletion(null, null);
    }

    @Override
    public void rollback(RecordChunk<Record> recordChunk, Throwable t) {
        fqChunk.getCallback().onCompletion(t, null);
    }

}
