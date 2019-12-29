package com.ucar.datalink.manager.core.web.util;

import com.google.common.collect.Sets;
import com.ucar.datalink.biz.meta.ElasticSearchUtil;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.KuduUtil;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.ModeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 映射配置工具类
 *
 * @author wenbin.song
 * @date 2019/02/22
 */
public class MediaMappingConfigUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static final Logger logger = LoggerFactory.getLogger(MediaMappingConfigUtil.class);

    /**
     * 配置映射时，校验源端数据源是myslq的表是否存在主键
     * @param sourceTableName
     * @param tableNameSet
     */
    public static void validateMysqlTablePk(MediaSourceInfo mediaSourceInfo, String[] sourceTableName, Set<String> tableNameSet) {

            if(mediaSourceInfo.getType()!= MediaSourceType.MYSQL){
                return;
            }
            try{
                getTableNameSet(mediaSourceInfo,sourceTableName,tableNameSet);
                Iterator<String> it = tableNameSet.iterator();
                while (it.hasNext()) {
                    String table = it.next();
                    if(!RDBMSUtil.hasPrimaryKey(mediaSourceInfo,table)){
                        throw new RuntimeException(String.format("源表%s没有主键", table));
                    }
                }
            } catch (Exception e) {
                logger.info("校验mysql源表主键时异常",e);
                throw new RuntimeException(e);
            }
    }

    /**
     * 配置映射时，校验目标端数据源是myslq的表是否存在主键
     * @param sourceTableNameSet
     * @param targetTableNames
     */
    public static void validateExistsTargetMedia(MediaSourceInfo mediaSourceInfo,String[] sourceTableName, Set<String> sourceTableNameSet, String[] targetTableNames) {
        try {
            if (mediaSourceInfo == null) {
                return;
            }
            Set<String> sourceTableSet = Sets.newHashSet(sourceTableName);
            Set<String> targetTableNameSet = null;
            if (sourceTableSet.contains("(.*)")) {
                targetTableNameSet = sourceTableNameSet;
            } else {
                targetTableNameSet = Sets.newHashSet(targetTableNames);
            }
            if(targetTableNameSet == null||targetTableNameSet.size()==0){
                return;
            }

            if (mediaSourceInfo.getType() == MediaSourceType.MYSQL || mediaSourceInfo.getType() == MediaSourceType.ORACLE
                    || mediaSourceInfo.getType() == MediaSourceType.SQLSERVER || mediaSourceInfo.getType() == MediaSourceType.POSTGRESQL) {
                List<String> tableList = RDBMSUtil.checkTargetTables(mediaSourceInfo, sourceTableName,targetTableNameSet);
                if (tableList != null && tableList.size() > 0) {
                    throw new RuntimeException(String.format("rdbms表[%s]在目标端数据库中不存在", StringUtils.join(tableList, ",")));
                }
            } else if (mediaSourceInfo.getType() == MediaSourceType.HBASE) {
                List<String> tableList = HBaseUtil.checkTargetTables(mediaSourceInfo, targetTableNameSet);
                if (tableList != null && tableList.size() > 0) {
                    throw new RuntimeException(String.format("hbase表[%s]在目标端数据库中不存在", StringUtils.join(tableList, ",")));
                }
            } else if (mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH) {
                List<String> indexList = ElasticSearchUtil.checkTargetIndexes(mediaSourceInfo, targetTableNameSet);
                if (indexList != null && indexList.size() > 0) {
                    throw new RuntimeException(String.format("目标端索引[%s]不存在", StringUtils.join(indexList, ",")));
                }
            } else if (mediaSourceInfo.getType() == MediaSourceType.KUDU) {
                List<String> tableList = KuduUtil.checkTargetTables(mediaSourceInfo, targetTableNameSet);
                if (tableList != null && tableList.size() > 0) {
                    throw new RuntimeException(String.format("kudu表[%s]在目标端数据库中不存在", StringUtils.join(tableList, ",")));
                }
            }

        } catch (Exception e) {
            logger.info("校验目标端表是否存在时异常", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取映射的表列表
     * @param mediaSourceInfo
     * @param sourceTableName
     * @param tableNameSet
     * @throws Exception
     */
    private static void getTableNameSet(MediaSourceInfo mediaSourceInfo, String[] sourceTableName,Set<String> tableNameSet) throws Exception {
        for (String table : sourceTableName) {
            MediaInfo.ModeValue modeValue = ModeUtils.parseMode(table);

            if (modeValue.getMode().isMulti()) {
                //分库分表获取第一个
                tableNameSet.add(modeValue.getSingleValue());
            } else if (modeValue.getMode().isMonthly()) {
                //按月分表取当前月份
                tableNameSet.add(ModeUtils.getMonthlyPrefix(table) + sdf.format(new java.util.Date()).substring(0, 6));
            } else if (modeValue.getMode().isYearly()) {
                //按年分表取当前年份
                tableNameSet.add(ModeUtils.getYearlyPrefix(table) + sdf.format(new Date()).substring(0, 4));
            } else if (modeValue.getMode().isWildCard()) {
                //(.*)取出所有的表
                if (mediaSourceInfo.getType() == MediaSourceType.MYSQL || mediaSourceInfo.getType() == MediaSourceType.ORACLE
                        || mediaSourceInfo.getType() == MediaSourceType.SQLSERVER || mediaSourceInfo.getType() == MediaSourceType.SDDL
                        || mediaSourceInfo.getType() == MediaSourceType.POSTGRESQL) {

                    List<String> tableNameList = RDBMSUtil.getTableName(mediaSourceInfo);
                    tableNameSet.addAll(tableNameList);
                }else if(mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH){
                    tableNameSet.add(table);
                }
            }else if(modeValue.getMode().isSingle()){
                tableNameSet.add(table);
            }
        }
    }

}
