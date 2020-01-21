package com.ucar.datalink.reader.fq;

import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.reader.fq.FqChunk;
import com.ucar.datalink.domain.plugin.reader.fq.FqReaderParameter;
import com.zuche.framework.metaq.handler.DefaultExecutorMessageListener;
import com.zuche.framework.metaq.vo.MessageVO;
import com.zuche.framework.remote.nio.codec.HessianSerializerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sqq on 2017/6/13.
 */
public class FqMessageListener extends DefaultExecutorMessageListener {
    public static final Logger LOGGER = LoggerFactory.getLogger(FqMessageListener.class);
    private ConcurrentHashMap<String, List<Record>> partitionBufferMap = new ConcurrentHashMap<>();
    public ArrayBlockingQueue<FqChunk> queue = new ArrayBlockingQueue<>(1);

    public FqMessageListener(ArrayBlockingQueue<FqChunk> queue){
        this.queue = queue;
    }

    @Override
    public void handlerMessage(MessageVO messageVO) {
        try {
            setAutoAck(messageVO.getPartition());
            Record record;
            List<Record> recordList = new ArrayList<>();
            List<Record> bufferList;
            FqReaderParameter fqReaderParameter = new FqReaderParameter();
            byte[] data = messageVO.getData();
            if(data != null && data.length > 0){
                record = (Record) HessianSerializerUtils.deserialize(data);
                recordList.add(record);
                partitionBufferMap.put(messageVO.getPartition(), recordList);
                LOGGER.error("fq消息：{}", record);
                bufferList = bufferedDataList(messageVO.getPartition(), recordList, fqReaderParameter.getBufferSize());
                if (bufferList == null) {
                    setAutoAck(messageVO.getPartition());
                    return;
                }
                FqChunk fqChunk = new FqChunk(bufferList, new FutureCallback());
                queue.put(fqChunk);
                fqChunk.getCallback().get();
                ack(messageVO.getPartition());
            }
        } catch (Exception e) {
            setRollback(messageVO.getPartition());
            LOGGER.error("fq消息消费异常", e);
        }
    }

    private List<Record> bufferedDataList(String partition, List<Record> recordList, Integer bufferSize){
        List<Record> dataList = partitionBufferMap.get(partition);
        if (dataList == null) {
            dataList = recordList;
        } else {
            dataList.addAll(recordList);
        }
        partitionBufferMap.put(partition, dataList);
        if (dataList.size() >= bufferSize) {
            partitionBufferMap.remove(partition);
            return dataList;
        }
        return null;
    }


}
