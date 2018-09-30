package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by csf on 17/4/7.
 */
@Service
public class MediaSourceServiceImpl implements MediaSourceService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MediaSourceServiceImpl.class);

    @Autowired
    private MediaSourceDAO mediaSourceDAO;

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private MediaDAO mediaDAO;

    @Override
    public List<MediaSourceInfo> getList() {
        return mediaSourceDAO.getList();
    }

    @Override
    public List<MediaSourceInfo> getListByType(Set<MediaSourceType> mediaSourceType) {
        return mediaSourceDAO.getListByType(mediaSourceType);
    }

    @Override
    public List<MediaSourceInfo> getListForQueryPage(@Param("mediaSourceType") Set<MediaSourceType> mediaSourceType, @Param("mediaSourceName") String mediaSourceName) {
        return mediaSourceDAO.getListForQueryPage(mediaSourceType, mediaSourceName);
    }

    @Override
    public Boolean insert(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ZOOKEEPER) {
            checkZkInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.SDDL) {
            checkSddlInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HDFS) {
            checkHDFSInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ELASTICSEARCH) {
            checkEsInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HBASE) {
            checkHBaseInfo(mediaSourceInfo);
        } else {
            checkRdbmsInfo(mediaSourceInfo);
        }
        Integer num;
        try {
            num = mediaSourceDAO.insert(mediaSourceInfo);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", mediaSourceInfo.getName()));
        }
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ZOOKEEPER) {
            checkZkInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.SDDL) {
            checkSddlInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HDFS) {
            checkHDFSInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ELASTICSEARCH) {
            checkEsInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HBASE) {
            checkHBaseInfo(mediaSourceInfo);
        } else {
            checkRdbmsInfo(mediaSourceInfo);
        }
        Integer num;
        try {
            num = mediaSourceDAO.update(mediaSourceInfo);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", mediaSourceInfo.getName()));
        }
        if (num >= 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        TaskInfo condition = new TaskInfo();
        condition.setReaderMediaSourceId(id);
        List<TaskInfo> taskInfos = taskDAO.listByCondition(condition);
        if (taskInfos != null && !taskInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在读端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(taskInfos.stream().map(t -> t.getId()).collect(Collectors.toList())))
            );
        }

        List<MediaMappingInfo> srcMappingInfos = mediaDAO.findMediaMappingsBySrcMediaSourceId(id);
        if (srcMappingInfos != null && !srcMappingInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在读端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(srcMappingInfos.stream().map(m -> m.getTaskId()).distinct().collect(Collectors.toList())))
            );
        }

        List<MediaMappingInfo> targetMappingInfos = mediaDAO.findMediaMappingsByTargetMediaSourceId(id);
        if (targetMappingInfos != null && !targetMappingInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在写端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(targetMappingInfos.stream().map(m -> m.getTaskId()).distinct().collect(Collectors.toList())))
            );
        }

        mediaDAO.deleteMediaByMediaSourceId(id);
        Integer num = mediaSourceDAO.delete(id);
        if (num >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public MediaSourceInfo getById(Long id) {
        return mediaSourceDAO.getById(id);
    }

    @Override
    public List<String> getRdbTableName(MediaSourceInfo info) {
        List<String> result = new LinkedList<>();
        result.add("(.*)");
        result.addAll(RDBMSUtil.getTableName(info));
        return result;
    }

    @Override
    public List<String> getHbaseTableName(MediaSourceInfo hbaseMediaSourceInfo) {
        return HBaseUtil.getTables(hbaseMediaSourceInfo).stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getRdbColumnName(MediaSourceInfo info, String tableName) {
        return RDBMSUtil.getColumnName(info,tableName);
    }

    @Override
    public void checkDbConnection(RdbMediaSrcParameter rdbMediaSrcParameter) throws Exception {
        RdbMediaSrcParameter.WriteConfig writeConfig = rdbMediaSrcParameter.getWriteConfig();
        RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
        RDBMSUtil.checkRdbConnection(
                rdbMediaSrcParameter.getMediaSourceType().name(),
                writeConfig.getWriteHost(),
                rdbMediaSrcParameter.getPort(),
                rdbMediaSrcParameter.getNamespace(),
                writeConfig.getUsername(),
                writeConfig.getPassword());
        List<String> hosts = readConfig.getHosts();
        if (hosts != null && hosts.size() > 0) {
            for (String ip : hosts) {
                RDBMSUtil.checkRdbConnection(
                        rdbMediaSrcParameter.getMediaSourceType().name(),
                        ip,
                        rdbMediaSrcParameter.getPort(),
                        rdbMediaSrcParameter.getNamespace(),
                        readConfig.getUsername(),
                        readConfig.getPassword());
            }
        }
        RDBMSUtil.checkRdbConnection(
                rdbMediaSrcParameter.getMediaSourceType().name(),
                readConfig.getEtlHost(),
                rdbMediaSrcParameter.getPort(),
                rdbMediaSrcParameter.getNamespace(),
                readConfig.getUsername(),
                readConfig.getPassword());
    }

    private void checkRdbmsInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namespace = mediaSourceInfo.getParameterObj().getNamespace();
        if (!msName.startsWith(namespace)) {
            throw new ValidationException(String.format("数据源名称必须以%s为前缀.", namespace + "_"));
        }

        MediaSrcParameter srcParameter = mediaSourceInfo.getParameterObj();
        if (srcParameter instanceof RdbMediaSrcParameter) {
            RdbMediaSrcParameter rdbMediaSrcParameter = (RdbMediaSrcParameter) srcParameter;
            RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
            if (readConfig.getHosts().size() < 2) {
                throw new ValidationException("读库数量必须大于等于两个.");
            }

            if (StringUtils.isBlank(readConfig.getEtlHost())) {
                throw new ValidationException("Etl-Host为必输项，请配置.");
            }
        }
    }

    private void checkSddlInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String prefix = "sddl_";
        if (!msName.startsWith(prefix)) {
            throw new ValidationException(String.format("数据源名称必须以%s为前缀.", prefix));
        }
    }

    private void checkZkInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "zk_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("zk数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        List<MediaSourceInfo> zkList = getListByType(setMediaSource);
        if (zkList != null) {
            zkList.forEach(zk -> {
                String servers = ((ZkMediaSrcParameter) zk.getParameterObj()).getServers();
                if (servers.equals(((ZkMediaSrcParameter) mediaSourceInfo.getParameterObj()).getServers()) && !zk.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("ip地址已经存在：%s.", servers));
                }
            });
        }
    }

    public void checkHDFSInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "hdfs_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("HDFS数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.HDFS);
        List<MediaSourceInfo> hdfsList = getListByType(setMediaSource);
        HDFSMediaSrcParameter hdfsParameter = mediaSourceInfo.getParameterObj();
        if (hdfsList != null) {
            hdfsList.forEach(hdfs -> {
                HDFSMediaSrcParameter hdfsPara = hdfs.getParameterObj();
                if (hdfsPara.getNameServices().equals(hdfsParameter.getNameServices()) && !hdfs.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("nameServices已经存在：%s.", hdfsPara.getNameServices()));
                }
            });
        }
    }

    public void checkEsInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "es_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("ES数据源名称必须以%s为前缀.", namePrefix));
        }
    }

    public void checkHBaseInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "hbase_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("HBase名称必须以%s为前缀.", namePrefix));
        }
    }


    @Override
    public Integer msCount() {
        return mediaSourceDAO.msCount();
    }

    @Override
    public List<StatisDetail> getCountByType() {
        return mediaSourceDAO.getCountByType();
    }
}
