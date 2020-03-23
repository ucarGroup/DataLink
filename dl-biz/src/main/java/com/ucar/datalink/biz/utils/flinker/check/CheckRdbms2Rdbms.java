package com.ucar.datalink.biz.utils.flinker.check;

import com.alibaba.fastjson.JSONArray;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.check.meta.ModifyCheckColumnInfo;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by yang.wang09 on 2018-05-17 16:21.
 * 关系型数据库 到 关系型数据库的转换关系时的检查
 * 用于检查JobConfigInfo中的json串中的列信息(旧的数据)，和最新获取到的列信息是否一致
 * 如果不一致返回最新的源列信息，目标端列信息
 * 包含 如下一些类型的检查
 * MySql -> Mysql
 * MySql -> SqlServer
 * MySql -> PostgreSql
 * SqlServer -> Mysql
 * SqlServer -> SqlServer
 * SqlServer -> PostgreSql
 * PostgreSql -> MySql
 * PostgreSql -> SqlServer
 * PostgreSql -> PostgreSql
 *
 */
public class CheckRdbms2Rdbms implements ICheck {

    private static Logger logger = LoggerFactory.getLogger(CheckRdbms2Rdbms.class);

    @Override
    public void check(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, JobConfigInfo info) throws Exception {
        DLConfig connConf = DLConfig.parseFrom(info.getJob_content());
        JSONArray array = (JSONArray)connConf.get("job.content[0].reader.parameter.column");
        ModifyCheckColumnInfo srcModifyInfo = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            srcModifyInfo.addName( array.getString(i) );
        }

        Set<String> set = new HashSet<>();
        List<ColumnMeta> columnMetas = SyncCheckUtil.getNewColumnInfo(srcInfo, info.getJob_media_name());
        for(ColumnMeta cm :columnMetas) {
            set.add(cm.getName());
        }
        logger.info("CheckRdbms2Rdbms -> "+set.toString());
        for(String name : srcModifyInfo.getName()) {
            if(set.contains(name)) {
                set.remove(name);
            }
        }
        if(set.size() == 0) {
            return;
        }

        MediaMeta mm = new MediaMeta();
        mm.setColumn(columnMetas);
        mm.setDbType(srcInfo.getType());
        mm.setName(srcInfo.getName());
        mm.setNameSpace(srcInfo.getParameterObj().getNamespace());
        MediaMeta transformMeta = MetaMapping.transformToRDBMS(mm);
        ModifyCheckColumnInfo destModifyInfo = new ModifyCheckColumnInfo();
        for(ColumnMeta cm : transformMeta.getColumn()) {
            destModifyInfo.addName(cm.getName());
        }
        srcModifyInfo.getName().clear();
        for(ColumnMeta cm : columnMetas) {
            srcModifyInfo.addName(cm.getName());
        }
        SyncCheckUtil.modifyJsonColumn(info, srcModifyInfo, destModifyInfo);
    }
}
