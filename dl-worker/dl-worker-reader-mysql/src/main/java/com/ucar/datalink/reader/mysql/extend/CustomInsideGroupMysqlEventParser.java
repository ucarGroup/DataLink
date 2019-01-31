package com.ucar.datalink.reader.mysql.extend;

import com.alibaba.otter.canal.parse.inbound.AbstractEventParser;
import com.alibaba.otter.canal.parse.inbound.ErosaConnection;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;

import java.util.Timer;

/**
 * 1. 当处于组模式时，每个分库用该类型的Parser
 * 2. 该Parser主要用来解决Canal自带的MysqlEventParser心跳频率最小只能1s的问题，具体描述如下：
 *    在组模式下心跳不仅仅用来探测DB的健康状态，还用来强制产生binlog，以解决某个或某几个分库没有binlog产生
 *    而导致所有库的同步都卡住的问题(原因可查看GroupEventSink的实现机制)。为了达到更快的同步速度，应该让心
 *    跳频率足够小(当然也不能太小)，这样才能源源不断的有binlog产生，MysqlEventParser支持的最小频率为1s,这个
 *    频率满足不了使用需求，举例来说：比如有8个分库，那么最差的情况每次需要等8s才能汇集到所有库的数据(当然这
 *    是一个比较极端的情况，但如果只有一两个库有零星的数据，按照1s的心跳频率，这些零星数据在概率上要延迟4-5s
 *    的时间才能被同步到目标端)。
 *    拿lucky-coffee的实际情况来举例：每天早晨门店陆续开店，晚上陆续闭店，一早一晚就会出现只有部分库有零星
 *    数据的情况，才这个时间段按照1s的心跳频率，大概就有3-5s的延迟
 * 3. startHeartBeat()方法的代码copy自AbstractEventParser，只修改了一行代码，见"@@changes"备注
 * 4. Important：对引用的canal版本进行升级的时候，记得进行代码检查和合并
 *
 * Created by lubiao on 2019/1/29.
 */
public class CustomInsideGroupMysqlEventParser extends MysqlEventParser{

    @Override
    protected void startHeartBeat(ErosaConnection connection) {
        lastEntryTime = 0L; // 初始化
        if (timer == null) {// lazy初始化一下
            String name = String.format("destination = %s , address = %s , HeartBeatTimeTask",
                    destination,
                    runningInfo == null ? null : runningInfo.getAddress().toString());
            synchronized (AbstractEventParser.class) {
                // synchronized (MysqlEventParser.class) {
                // why use MysqlEventParser.class, u know, MysqlEventParser is
                // the child class 4 AbstractEventParser,
                // do this is ...
                if (timer == null) {
                    timer = new Timer(name, true);
                }
            }
        }

        if (heartBeatTimerTask == null) {// fixed issue #56，避免重复创建heartbeat线程
            heartBeatTimerTask = buildHeartBeatTimeTask(connection);
            Integer interval = detectingIntervalInSeconds;
            timer.schedule(heartBeatTimerTask, 10L, interval * 10L);//@@changes
            logger.info("start heart beat.... ");
        }
    }
}
