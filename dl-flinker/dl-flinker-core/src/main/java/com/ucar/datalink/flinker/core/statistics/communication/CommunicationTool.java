package com.ucar.datalink.flinker.core.statistics.communication;

import com.ucar.datalink.flinker.api.statistics.PerfTrace;
import com.ucar.datalink.flinker.api.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.Validate;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 这里主要是业务层面的处理
 */
public final class CommunicationTool {
    public static final String STAGE = "stage";
    public static final String BYTE_SPEED = "byteSpeed";
    public static final String RECORD_SPEED = "recordSpeed";
    public static final String PERCENTAGE = "percentage";

    public static final String READ_SUCCEED_RECORDS = "readSucceedRecords";
    public static final String READ_SUCCEED_BYTES = "readSucceedBytes";

    public static final String READ_FAILED_RECORDS = "readFailedRecords";
    public static final String READ_FAILED_BYTES = "readFailedBytes";

    public static final String WRITE_RECEIVED_RECORDS = "writeReceivedRecords";
    public static final String WRITE_RECEIVED_BYTES = "writeReceivedBytes";

    public static final String WRITE_FAILED_RECORDS = "writeFailedRecords";
    public static final String WRITE_FAILED_BYTES = "writeFailedBytes";

    public static final String TOTAL_READ_RECORDS = "totalReadRecords";
    public static final String TOTAL_READ_BYTES = "totalReadBytes";

    public static final String TOTAL_ERROR_RECORDS = "totalErrorRecords";
    public static final String TOTAL_ERROR_BYTES = "totalErrorBytes";

    public static final String WRITE_SUCCEED_RECORDS = "writeSucceedRecords";
    public static final String WRITE_SUCCEED_BYTES = "writeSucceedBytes";

    public static final String WAIT_WRITER_TIME = "waitWriterTime";

    public static final String WAIT_READER_TIME = "waitReaderTime";

    public static Communication getReportCommunication(Communication now, Communication old, int totalStage) {
        Validate.isTrue(now != null && old != null,
                "为汇报准备的新旧metric不能为null");

        long totalReadRecords = getTotalReadRecords(now);
        long totalReadBytes = getTotalReadBytes(now);
        now.setLongCounter(TOTAL_READ_RECORDS, totalReadRecords);
        now.setLongCounter(TOTAL_READ_BYTES, totalReadBytes);
        now.setLongCounter(TOTAL_ERROR_RECORDS, getTotalErrorRecords(old));
        now.setLongCounter(TOTAL_ERROR_BYTES, getTotalErrorBytes(old));
        now.setLongCounter(WRITE_SUCCEED_RECORDS, getWriteSucceedRecords(old));
        now.setLongCounter(WRITE_SUCCEED_BYTES, getWriteSucceedBytes(old));

        long timeInterval = now.getTimestamp() - old.getTimestamp();
        long sec = timeInterval <= 1000 ? 1 : timeInterval / 1000;
        long bytesSpeed = (totalReadBytes
                - getTotalReadBytes(old)) / sec;
        long recordsSpeed = (totalReadRecords
                - getTotalReadRecords(old)) / sec;

        now.setLongCounter(BYTE_SPEED, bytesSpeed < 0 ? 0 : bytesSpeed);
        now.setLongCounter(RECORD_SPEED, recordsSpeed < 0 ? 0 : recordsSpeed);
        now.setDoubleCounter(PERCENTAGE, now.getLongCounter(STAGE) / (double) totalStage);
        //now.setState(old.getState());
        if (old.getThrowable() != null) {
            now.setThrowable(old.getThrowable());
        }

        return now;
    }

    public static long getTotalReadRecords(final Communication communication) {
        return communication.getLongCounter(READ_SUCCEED_RECORDS) +
                communication.getLongCounter(READ_FAILED_RECORDS);
    }

    public static long getTotalReadBytes(final Communication communication) {
        return communication.getLongCounter(READ_SUCCEED_BYTES) +
                communication.getLongCounter(READ_FAILED_BYTES);
    }

    public static long getTotalErrorRecords(final Communication communication) {
        return communication.getLongCounter(READ_FAILED_RECORDS) +
                communication.getLongCounter(WRITE_FAILED_RECORDS);
    }

    public static long getTotalErrorBytes(final Communication communication) {
        return communication.getLongCounter(READ_FAILED_BYTES) +
                communication.getLongCounter(WRITE_FAILED_BYTES);
    }

    public static long getWriteSucceedRecords(final Communication communication) {
        return communication.getLongCounter(WRITE_RECEIVED_RECORDS) -
                communication.getLongCounter(WRITE_FAILED_RECORDS);
    }

    public static long getWriteSucceedBytes(final Communication communication) {
        return communication.getLongCounter(WRITE_RECEIVED_BYTES) -
                communication.getLongCounter(WRITE_FAILED_BYTES);
    }

    public static class Stringify {
        private final static DecimalFormat df = new DecimalFormat("0.00");

        public static String getSnapshot(final Communication communication) {
            StringBuilder sb = new StringBuilder();
            sb.append("Total ");
            sb.append(getTotal(communication));
            sb.append(" | ");
            sb.append("Speed ");
            sb.append(getSpeed(communication));
            sb.append(" | ");
            sb.append("Error ");
            sb.append(getError(communication));
            sb.append(" | ");
            sb.append(" All Task WaitWriterTime ");
            sb.append(PerfTrace.unitTime(communication.getLongCounter(WAIT_WRITER_TIME)));
            sb.append(" | ");
            sb.append(" All Task WaitReaderTime ");
            sb.append(PerfTrace.unitTime(communication.getLongCounter(WAIT_READER_TIME)));
            sb.append(" | ");
            sb.append("Percentage ");
            sb.append(getPercentage(communication));
            return sb.toString();
        }

        private static String getTotal(final Communication communication) {
            return String.format("%d records, %d bytes",
                    communication.getLongCounter(TOTAL_READ_RECORDS),
                    communication.getLongCounter(TOTAL_READ_BYTES));
        }

        private static String getSpeed(final Communication communication) {
            return String.format("%s/s, %d records/s",
                    StrUtil.stringify(communication.getLongCounter(BYTE_SPEED)),
                    communication.getLongCounter(RECORD_SPEED));
        }

        private static String getError(final Communication communication) {
            return String.format("%d records, %d bytes",
                    communication.getLongCounter(TOTAL_ERROR_RECORDS),
                    communication.getLongCounter(TOTAL_ERROR_BYTES));
        }

        private static String getPercentage(final Communication communication) {
            return df.format(communication.getDoubleCounter(PERCENTAGE) * 100) + "%";
        }
    }

    public static class Jsonify {
        @SuppressWarnings("rawtypes")
        public static String getSnapshot(Communication communication) {
            Validate.notNull(communication);

            Map<String, Object> state = new HashMap<String, Object>();

            Pair pair = getTotalBytes(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getTotalRecords(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getSpeedRecord(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getSpeedByte(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getStage(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getErrorRecords(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getErrorBytes(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getErrorMessage(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getPercentage(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getWaitReaderTime(communication);
            state.put((String) pair.getKey(), pair.getValue());

            pair = getWaitWriterTime(communication);
            state.put((String) pair.getKey(), pair.getValue());

            return JSON.toJSONString(state);
        }

        private static Pair<String, Long> getTotalBytes(final Communication communication) {
            return new Pair<String, Long>("totalBytes", communication.getLongCounter(TOTAL_READ_BYTES));
        }

        private static Pair<String, Long> getTotalRecords(final Communication communication) {
            return new Pair<String, Long>("totalRecords", communication.getLongCounter(TOTAL_READ_RECORDS));
        }

        private static Pair<String, Long> getSpeedByte(final Communication communication) {
            return new Pair<String, Long>("speedBytes", communication.getLongCounter(BYTE_SPEED));
        }

        private static Pair<String, Long> getSpeedRecord(final Communication communication) {
            return new Pair<String, Long>("speedRecords", communication.getLongCounter(RECORD_SPEED));
        }

        private static Pair<String, Long> getErrorRecords(final Communication communication) {
            return new Pair<String, Long>("errorRecords", communication.getLongCounter(TOTAL_ERROR_RECORDS));
        }

        private static Pair<String, Long> getErrorBytes(final Communication communication) {
            return new Pair<String, Long>("errorBytes", communication.getLongCounter(TOTAL_ERROR_BYTES));
        }

        private static Pair<String, Long> getStage(final Communication communication) {
            return new Pair<String, Long>("stage", communication.getLongCounter(STAGE));
        }

        private static Pair<String, Double> getPercentage(final Communication communication) {
            return new Pair<String, Double>("percentage", communication.getDoubleCounter(PERCENTAGE));
        }

        private static Pair<String, String> getErrorMessage(final Communication communication) {
            return new Pair<String, String>("errorMessage", communication.getThrowableMessage());
        }

        private static Pair<String, Long> getWaitReaderTime(final Communication communication) {
            return new Pair<String, Long>("waitReaderTime", communication.getLongCounter(CommunicationTool.WAIT_READER_TIME));
        }

        private static Pair<String, Long> getWaitWriterTime(final Communication communication) {
            return new Pair<String, Long>("waitWriterTime", communication.getLongCounter(CommunicationTool.WAIT_WRITER_TIME));
        }

        static class Pair<K, V> {
            public Pair(final K key, final V value) {
                this.key = key;
                this.value = value;
            }

            public K getKey() {
                return key;
            }

            public V getValue() {
                return value;
            }

            private K key;

            private V value;
        }
    }
}
