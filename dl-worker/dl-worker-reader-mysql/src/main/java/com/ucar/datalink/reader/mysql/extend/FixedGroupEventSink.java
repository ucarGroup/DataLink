package com.ucar.datalink.reader.mysql.extend;

import com.alibaba.otter.canal.sink.entry.EntryEventSink;
import com.alibaba.otter.canal.sink.entry.group.GroupBarrier;
import com.alibaba.otter.canal.sink.entry.group.TimelineBarrier;
import com.alibaba.otter.canal.store.model.Event;

import java.util.Arrays;
import java.util.List;

/**
 * 1. copy自com.alibaba.otter.canal.sink.entry.group.GroupEventSink(版本：1.0.24)
 * 2. 不同点一：使用了FixedTimelineTransactionBarrier类，而不是canal自带的TimelineTransactionBarrier
 * 3. 不同点二：修复了doSink里的一个bug，bug描述参见 https://github.com/alibaba/canal/issues/1478
 *
 * Created by lubiao on 2017/10/10.
 */
public class FixedGroupEventSink extends EntryEventSink {

    private int          groupSize;
    private GroupBarrier barrier;  // 归并排序需要预先知道组的大小，用于判断是否组内所有的sink都已经开始正常取数据

    public FixedGroupEventSink(){
        this(1);
    }

    public FixedGroupEventSink(int groupSize){
        super();
        this.groupSize = groupSize;
    }

    public void start() {
        super.start();

        if (filterTransactionEntry) {
            barrier = new TimelineBarrier(groupSize);
        } else {
            barrier = new FixedTimelineTransactionBarrier(groupSize);// 支持事务保留
        }
    }

    protected boolean doSink(List<Event> events) {
        int size = events.size();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            try {
                barrier.await(event);// 进行timeline的归并调度处理
                if (filterTransactionEntry) {
                    super.doSink(Arrays.asList(event));
                } else if (i == size - 1) {
                    // 针对事务数据，只有到最后一条数据都通过后，才进行sink操作，保证原子性
                    // 同时批量sink，也要保证在最后一条数据释放状态之前写出数据，否则就有并发问题
                    return super.doSink(events);
                }
            } catch (InterruptedException e) {
                return false;
            } finally {
                barrier.clear(event);
            }
        }

        return false;
    }

    public void interrupt() {
        super.interrupt();
        barrier.interrupt();
    }
}
