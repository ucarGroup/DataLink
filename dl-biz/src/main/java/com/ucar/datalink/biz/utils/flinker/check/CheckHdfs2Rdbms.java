package com.ucar.datalink.biz.utils.flinker.check;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.check.meta.ModifyCheckColumnInfo;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yang.wang09 on 2018-05-18 11:15.
 */
public class CheckHdfs2Rdbms implements ICheck {

    @Override
    public void check(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, JobConfigInfo info) throws Exception {
        String json = info.getJob_content();
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].reader.parameter.column");
        ModifyCheckColumnInfo oldColumns = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            JSONObject jo = (JSONObject)array.get(i);
            oldColumns.addIndexType(jo.getString("index"), jo.getString("type"));
        }
        //获取最新的Hive列信息
        List<ColumnMeta> columns = SyncCheckUtil.getNewColumnInfo(srcInfo, info.getJob_media_name());
        MediaMeta mm = new MediaMeta();
        mm.setColumn(columns);
        mm.setDbType(srcInfo.getType());
        mm.setName(srcInfo.getName());
        mm.setNameSpace(srcInfo.getParameterObj().getNamespace());
        MediaMeta transformMeta = MetaMapping.transformToRDBMS(mm);
        List<ColumnMeta> transformColumns = transformMeta.getColumn();
        Map<String,ColumnMeta> map = new ConcurrentHashMap<>();
        for(ColumnMeta cm : columns) {
            map.put(cm.getName(),cm);
        }

        //addColumns    增加的列信息
        //modifyColumns 修改的列信息
        List<ModifyCheckColumnInfo.IndexTye> nameTypes = oldColumns.getIndexType();
        List<ColumnMeta> addColumns = new ArrayList<>();
        List<ColumnMeta> modifyColumns = new ArrayList<>();
        for(ModifyCheckColumnInfo.IndexTye nt : nameTypes) {
            if( map.containsKey(nt.index) ) {
                ColumnMeta cm = map.get(nt.index);
                if( !nt.type.equals(cm.getType()) ) {
                    modifyColumns.add(map.get(nt.index));
                }
                map.remove(nt.index);
            }
        }

        for(Iterator<Map.Entry<String,ColumnMeta>> iter=map.entrySet().iterator();iter.hasNext();) {
            addColumns.add(iter.next().getValue());
        }
        if(addColumns.size()==0 && modifyColumns.size()==0) {
            return;
        }

        MediaMeta meta = new MediaMeta();
        meta.setDbType( srcInfo.getParameterObj().getMediaSourceType() );
        meta.setName( info.getJob_media_name() );
        meta.setNameSpace( destInfo.getParameterObj().getNamespace() );
        meta.setColumn( columns );
        MediaMeta hdfsMediaMeta = MetaMapping.transformToDataX(meta);
        columns = hdfsMediaMeta.getColumn();
        ModifyCheckColumnInfo srcModifyColumnInfo = new ModifyCheckColumnInfo();
        for(ColumnMeta cm : columns) {
            srcModifyColumnInfo.addIndexType(cm.getName(),cm.getType());
        }
        ModifyCheckColumnInfo destModifyColumnInfo = new ModifyCheckColumnInfo();
        for(ColumnMeta cm : transformColumns) {
            destModifyColumnInfo.addName(cm.getName());
        }

        SyncCheckUtil.modifyJsonColumn(info, srcModifyColumnInfo, destModifyColumnInfo);
    }
}
