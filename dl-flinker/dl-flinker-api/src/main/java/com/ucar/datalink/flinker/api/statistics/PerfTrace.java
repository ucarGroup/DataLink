package com.ucar.datalink.flinker.api.statistics;

import com.ucar.datalink.flinker.api.base.TaskInfo;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * PerfTrace 记录 job（local模式），taskGroup（distribute模式），因为这2种都是jvm，即一个jvm里只需要有1个PerfTrace。
 */

public class PerfTrace {

    private static Logger LOG = LoggerFactory.getLogger(PerfTrace.class);
    private static PerfTrace instance;
    private static final Object lock = new Object();
    private String perfTraceId;
    private volatile boolean enable;
    private volatile boolean isJob;
    private long jobId;
    private long timingJobId;
    private int priority;
    private int batchSize = 500;
    private volatile boolean perfReportEnalbe = true;

    //jobid_jobversion,instanceid,taskid, src_mark, dst_mark,
    private Map<Integer, String> taskDetails = new ConcurrentHashMap<Integer, String>();
    //PHASE => PerfRecord
    private ConcurrentHashMap<PerfRecord.PHASE, SumPerfRecord> perfRecordMaps = new ConcurrentHashMap<PerfRecord.PHASE, SumPerfRecord>();
    private Configuration jobInfo;
    private final List<PerfRecord> startReportPool = new ArrayList<PerfRecord>();
    private final List<PerfRecord> endReportPool = new ArrayList<PerfRecord>();
    private final List<PerfRecord> totalEndReport = new ArrayList<PerfRecord>();
    private final Set<PerfRecord> waitingReportSet = new HashSet<PerfRecord>();


    //收集task任务信息
    private final Map<Integer, TaskInfo> taskCommunicationMap = new ConcurrentHashMap<Integer, TaskInfo>();

    //原始job json 信息
    private String originalConfiguration;

    public long getTimingJobId() {
        return timingJobId;
    }

    public void setTimingJobId(long timingJobId) {
        this.timingJobId = timingJobId;
    }

    public String getOriginalConfiguration() {
        return originalConfiguration;
    }

    public void setOriginalConfiguration(String originalConfiguration) {
        this.originalConfiguration = originalConfiguration;
    }

    public Map<Integer, TaskInfo> getTaskCommunicationMap() {
        return taskCommunicationMap;
    }

    /**
     * 单实例
     *
     * @param isJob
     * @param jobId
     * @param taskGroupId
     * @return
     */
    public static PerfTrace getInstance(boolean isJob, long jobId, int taskGroupId,int priority, boolean enable) {

        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new PerfTrace(isJob, jobId, taskGroupId,priority, enable);
                }
            }
        }
        return instance;
    }

    /**
     * 因为一个JVM只有一个，因此在getInstance(isJob,jobId,taskGroupId)调用完成实例化后，方便后续调用，直接返回该实例
     *
     * @return
     */
    public static PerfTrace getInstance() {
        if (instance == null) {
            LOG.error("PerfTrace instance not be init! must have some error! ");
            synchronized (lock) {
                if (instance == null) {
                    instance = new PerfTrace(false, -1111, -1111, 0, false);
                }
            }
        }
        return instance;
    }

    private PerfTrace(boolean isJob, long jobId, int taskGroupId, int priority, boolean enable) {
        this.perfTraceId = isJob ? "job_" + jobId : String.format("taskGroup_%s_%s", jobId, taskGroupId);
        this.enable = enable;
        this.isJob = isJob;
        this.jobId = jobId;
        this.priority = priority;
        LOG.info(String.format("PerfTrace traceId=%s, isEnable=%s, priority=%s", this.perfTraceId, this.enable, this.priority));

    }

    public void addTaskDetails(int taskId, String detail) {
        if (enable) {
            String before = "";
            int index = detail.indexOf("?");
            String current = detail.substring(0, index == -1 ? detail.length() : index);
            if(current.indexOf("[")>=0){
                current+="]";
            }
            if (taskDetails.containsKey(taskId)) {
                before = taskDetails.get(taskId).trim();
            }
            if (StringUtils.isEmpty(before)) {
                before = "";
            } else {
                before += ",";
            }
            this.taskDetails.put(taskId, before + current);
        }
    }

    public void tracePerfRecord(PerfRecord perfRecord) {
        if (enable) {
            //ArrayList非线程安全
            switch (perfRecord.getAction()) {
                case start:
                    synchronized (startReportPool) {
                        startReportPool.add(perfRecord);
                    }
                    break;
                case end:
                    synchronized (endReportPool) {
                        endReportPool.add(perfRecord);
                    }
                    break;
            }
        }
    }

    public String summarizeNoException(){
        String res;
        try {
            res = summarize();
        } catch (Exception e) {
            res = "PerfTrace summarize has Exception "+e.getMessage();
        }
        return res;
    }

    //任务结束时，对当前的perf总汇总统计
    private synchronized String summarize() {
        if (!enable) {
            return "PerfTrace not enable!";
        }

        if (totalEndReport.size() > 0) {
            sumEndPerfRecords(totalEndReport);
        }

        StringBuilder info = new StringBuilder();
        info.append("\n === total summarize info === \n");
        info.append("\n   1. all phase average time info and max time task info: \n\n");
        info.append(String.format("%-20s | %18s | %18s | %18s | %18s | %-100s\n", "PHASE", "AVERAGE USED TIME", "ALL TASK NUM", "MAX USED TIME", "MAX TASK ID", "MAX TASK INFO"));

        List<PerfRecord.PHASE> keys = new ArrayList<PerfRecord.PHASE>(perfRecordMaps.keySet());
        Collections.sort(keys, new Comparator<PerfRecord.PHASE>() {
            @Override
            public int compare(PerfRecord.PHASE o1, PerfRecord.PHASE o2) {
                return o1.toInt() - o2.toInt();
            }
        });
        for (PerfRecord.PHASE phase : keys) {
            SumPerfRecord sumPerfRecord = perfRecordMaps.get(phase);
            if (sumPerfRecord == null) {
                continue;
            }
            long averageTime = sumPerfRecord.getAverageTime();
            long maxTime = sumPerfRecord.getMaxTime();
            int maxTaskId = sumPerfRecord.maxTaskId;
            int maxTaskGroupId = sumPerfRecord.getMaxTaskGroupId();
            info.append(String.format("%-20s | %18s | %18s | %18s | %18s | %-100s\n",
                    phase, unitTime(averageTime), sumPerfRecord.totalCount, unitTime(maxTime), jobId + "-" + maxTaskGroupId + "-" + maxTaskId, taskDetails.get(maxTaskId)));
        }

        SumPerfRecord countSumPerf = Optional.fromNullable(perfRecordMaps.get(PerfRecord.PHASE.READ_TASK_DATA)).or(new SumPerfRecord());

        long averageRecords = countSumPerf.getAverageRecords();
        long averageBytes = countSumPerf.getAverageBytes();
        long maxRecord = countSumPerf.getMaxRecord();
        long maxByte = countSumPerf.getMaxByte();
        int maxTaskId4Records = countSumPerf.getMaxTaskId4Records();
        int maxTGID4Records = countSumPerf.getMaxTGID4Records();

        info.append("\n\n 2. record average count and max count task info :\n\n");
        info.append(String.format("%-20s | %18s | %18s | %18s | %18s | %18s | %-100s\n", "PHASE", "AVERAGE RECORDS", "AVERAGE BYTES", "MAX RECORDS", "MAX RECORD`S BYTES", "MAX TASK ID", "MAX TASK INFO"));
        if (maxTaskId4Records > -1) {
            info.append(String.format("%-20s | %18s | %18s | %18s | %18s | %18s | %-100s\n"
                    , PerfRecord.PHASE.READ_TASK_DATA, averageRecords, unitSize(averageBytes), maxRecord, unitSize(maxByte), jobId + "-" + maxTGID4Records + "-" + maxTaskId4Records, taskDetails.get(maxTaskId4Records)));

        }
        return info.toString();
    }

    //缺省传入的时间是nano
    public static String unitTime(long time) {
        return unitTime(time, TimeUnit.NANOSECONDS);
    }

    public static String unitTime(long time, TimeUnit timeUnit) {
        return String.format("%,.3fs", ((float) timeUnit.toNanos(time)) / 1000000000);
    }

    public static String unitSize(long size) {
        if (size > 1000000000) {
            return String.format("%,.2fG", (float) size / 1000000000);
        } else if (size > 1000000) {
            return String.format("%,.2fM", (float) size / 1000000);
        } else if (size > 1000) {
            return String.format("%,.2fK", (float) size / 1000);
        } else {
            return size + "B";
        }
    }


    public synchronized ConcurrentHashMap<PerfRecord.PHASE, SumPerfRecord> getPerfRecordMaps() {
        synchronized (endReportPool) {
            // perfRecordMaps.get(perfRecord.getPhase()).add(perfRecord);
            waitingReportSet.addAll(endReportPool);
            totalEndReport.addAll(endReportPool);
            endReportPool.clear();
        }
        if(totalEndReport.size() > 0 ){
            sumEndPerfRecords(totalEndReport);
        }
        return perfRecordMaps;
    }

    public List<PerfRecord> getWaitingReportList() {
        return new ArrayList<PerfRecord>(waitingReportSet);
    }

    public List<PerfRecord> getStartReportPool() {
        return startReportPool;
    }

    public List<PerfRecord> getEndReportPool() {
        return endReportPool;
    }

    public List<PerfRecord> getTotalEndReport() {
        return totalEndReport;
    }

    public Map<Integer, String> getTaskDetails() {
        return taskDetails;
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isJob() {
        return isJob;
    }

    public long getJobId() {
        return jobId;
    }

    private String cluster;
    private String jobDomain;
    private String srcType;
    private String dstType;
    private String srcGuid;
    private String dstGuid;
    private String dataxType;

    public void setJobInfo(Configuration jobInfo) {
        this.jobInfo = jobInfo;
        if (jobInfo != null) {
            cluster = jobInfo.getString("cluster");

            String srcDomain = jobInfo.getString("srcDomain", "null");
            String dstDomain = jobInfo.getString("dstDomain", "null");
            jobDomain = srcDomain + "|" + dstDomain;
            srcType = jobInfo.getString("srcType");
            dstType = jobInfo.getString("dstType");
            srcGuid = jobInfo.getString("srcGuid");
            dstGuid = jobInfo.getString("dstGuid");
            long jobId = jobInfo.getLong("jobId");
            if (jobId > 0) {
                //同步中心任务
                dataxType = "dsc";
            } else {
                dataxType = "datax3";
            }
        } else {
            dataxType = "datax3";
        }
    }

    public Configuration getJobInfo() {
        return jobInfo;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setPerfReportEnalbe(boolean perfReportEnalbe) {
        this.perfReportEnalbe = perfReportEnalbe;
    }


    private void sumEndPerfRecords(List<PerfRecord> totalEndReport) {
        if (!enable || totalEndReport == null) {
            return;
        }

        for (PerfRecord perfRecord : totalEndReport) {
            perfRecordMaps.putIfAbsent(perfRecord.getPhase(), new SumPerfRecord());
            perfRecordMaps.get(perfRecord.getPhase()).add(perfRecord);
        }

        totalEndReport.clear();
    }



    public static class SumPerfRecord {
        private long perfTimeTotal = 0;
        private long averageTime = 0;
        private long maxTime = 0;
        private int maxTaskId = -1;
        private int maxTaskGroupId = -1;
        private int totalCount = 0;

        private long recordsTotal = 0;
        private long sizesTotal = 0;
        private long averageRecords = 0;
        private long averageBytes = 0;
        private long maxRecord = 0;
        private long maxByte = 0;
        private int maxTaskId4Records = -1;
        private int maxTGID4Records = -1;

        synchronized void add(PerfRecord perfRecord) {
            if (perfRecord == null) {
                return;
            }
            perfTimeTotal += perfRecord.getElapsedTimeInNs();
            if (perfRecord.getElapsedTimeInNs() > maxTime) {
                maxTime = perfRecord.getElapsedTimeInNs();
                maxTaskId = perfRecord.getTaskId();
                maxTaskGroupId = perfRecord.getTaskGroupId();
            }

            recordsTotal += perfRecord.getCount();
            sizesTotal += perfRecord.getSize();
            if (perfRecord.getCount() > maxRecord) {
                maxRecord = perfRecord.getCount();
                maxByte = perfRecord.getSize();
                maxTaskId4Records = perfRecord.getTaskId();
                maxTGID4Records = perfRecord.getTaskGroupId();
            }

            totalCount++;
        }

        public long getPerfTimeTotal() {
            return perfTimeTotal;
        }

        public long getAverageTime() {
            if (totalCount > 0) {
                averageTime = perfTimeTotal / totalCount;
            }
            return averageTime;
        }

        public long getMaxTime() {
            return maxTime;
        }

        public int getMaxTaskId() {
            return maxTaskId;
        }

        public int getMaxTaskGroupId() {
            return maxTaskGroupId;
        }

        public long getRecordsTotal() {
            return recordsTotal;
        }

        public long getSizesTotal() {
            return sizesTotal;
        }

        public long getAverageRecords() {
            if (totalCount > 0) {
                averageRecords = recordsTotal / totalCount;
            }
            return averageRecords;
        }

        public long getAverageBytes() {
            if (totalCount > 0) {
                averageBytes = sizesTotal / totalCount;
            }
            return averageBytes;
        }

        public long getMaxRecord() {
            return maxRecord;
        }

        public long getMaxByte() {
            return maxByte;
        }

        public int getMaxTaskId4Records() {
            return maxTaskId4Records;
        }

        public int getMaxTGID4Records() {
            return maxTGID4Records;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }
}
