package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lubiao on 2017/9/28.
 */
public class MessageDumper {
    protected final static Logger logger = LoggerFactory.getLogger(MessageDumper.class);

    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected static final String SEP = SystemUtils.LINE_SEPARATOR;

    protected static final String context_format;

    protected static final String row_format;

    protected static final String transaction_format;

    static {
        context_format = SEP
                + "****************************************************" + SEP
                + "* Batch Id: [{}] ,count : [{}] , memsize : [{}] , Time : {}" + SEP
                + "* Start : [{}] " + SEP
                + "* End : [{}] " + SEP
                + "****************************************************" + SEP;

        row_format = SEP
                + "----------------> binlog[{}:{}] , name[{},{}] , eventType : {} , executeTime : {} , delay : {}ms"
                + SEP;

        transaction_format = SEP
                + "================> binlog[{}:{}] , executeTime : {} , delay : {}ms"
                + SEP;

    }

    public static void dumpMessages(Message message, long batchId, int size) {
        long memsize = 0;
        for (CanalEntry.Entry entry : message.getEntries()) {
            memsize += entry.getHeader().getEventLength();
        }

        String startPosition = null;
        String endPosition = null;
        if (message.getEntries()!=null && message.getEntries().size()>0) {
            startPosition = buildPositionForDump(message.getEntries()
                    .get(0));
            endPosition = buildPositionForDump(message.getEntries().get(
                    message.getEntries().size() - 1));
        }

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        logger.info(context_format, new Object[] { batchId, size, memsize,
                format.format(new Date()), startPosition, endPosition });
    }

    private static String buildPositionForDump(CanalEntry.Entry entry) {
        long time = entry.getHeader().getExecuteTime();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return entry.getHeader().getLogfileName() + ":"
                + entry.getHeader().getLogfileOffset() + ":"
                + entry.getHeader().getExecuteTime() + "("
                + format.format(date) + ")";
    }
}
