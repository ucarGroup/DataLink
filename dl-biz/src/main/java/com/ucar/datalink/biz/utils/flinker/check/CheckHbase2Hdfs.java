package com.ucar.datalink.biz.utils.flinker.check;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.check.meta.HBaseColumnInfo;
import com.ucar.datalink.biz.utils.flinker.check.meta.ModifyCheckColumnInfo;
import com.ucar.datalink.biz.utils.flinker.check.meta.SyncModifyTableInfo;
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
 * Created by yang.wang09 on 2018-05-18 11:08.
 */
public class CheckHbase2Hdfs implements ICheck {

    @Override
    public void check(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, JobConfigInfo info) throws Exception {

        String json = info.getJob_content();
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].writer.parameter.column");
        ModifyCheckColumnInfo oldColumns = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            JSONObject jo = (JSONObject)array.get(i);
            oldColumns.addNameType(jo.getString("name"), jo.getString("type"));
        }

        //获取最新的Hive列信息
        List<ColumnMeta> columns = HBaseUtil.getColumns(srcInfo, SyncCheckUtil.parseJobMediaName(info.getJob_media_name()));
        columns.forEach(c->{
            c.setName(c.getColumnFamily()+":"+c.getName());
        });
        MediaMeta mm = new MediaMeta();
        mm.setColumn(columns);
        mm.setDbType(srcInfo.getType());
        mm.setName(srcInfo.getName());

        mm.setNameSpace(srcInfo.getParameterObj().getNamespace());
        MediaMeta transformMeta = MetaMapping.transformToHDFS(mm);
        List<ColumnMeta> transformColumns = transformMeta.getColumn();
        Map<String,ColumnMeta> map = new ConcurrentHashMap<>();
        for(ColumnMeta cm : columns) {
            map.put(cm.getName(),cm);
        }

        //addColumns    增加的列信息
        //modifyColumns 修改的列信息
        List<ModifyCheckColumnInfo.NameType> nameTypes = oldColumns.getNameType();
        List<ColumnMeta> addColumns = new ArrayList<>();
        List<ColumnMeta> modifyColumns = new ArrayList<>();
        for(ModifyCheckColumnInfo.NameType nt : nameTypes) {
            if( map.containsKey(nt.name) ) {
                ColumnMeta cMeta = map.get(nt.name);
                String hiveType = SyncCheckUtil.parseColumnToHiveType(mm, cMeta);
                if(!hiveType.equals(nt.type)) {
                    modifyColumns.add(map.get(nt.name));
                }
                map.remove(nt.name);
            }
        }
        for(Iterator<Map.Entry<String,ColumnMeta>> iter=map.entrySet().iterator();iter.hasNext();) {
            addColumns.add(iter.next().getValue());
        }

        if(addColumns.size()==0 && modifyColumns.size()==0) {
            return;
        }
        ModifyCheckColumnInfo srcModifyColumnInfo = new ModifyCheckColumnInfo();
        for(ColumnMeta cm : columns) {
            srcModifyColumnInfo.addNameType(cm.getName(),cm.getType());
        }
        ModifyCheckColumnInfo destModifyColumnInfo = new ModifyCheckColumnInfo();
        for(ColumnMeta cm : transformColumns) {
            destModifyColumnInfo.addNameType(cm.getName(),cm.getType());
        }
        SyncCheckUtil.modifyJsonColumn(info, srcModifyColumnInfo, destModifyColumnInfo);

        //发送修改到 CDSE
        SyncModifyTableInfo add = new SyncModifyTableInfo();
        add.setDbType(srcInfo.getType().name());
        add.setDatabase(srcInfo.getName());
        add.setTable(info.getJob_media_name());
        HBaseColumnInfo[] addColumn = new HBaseColumnInfo[addColumns.size()];
        //将所有增加的列(ColumnMeta)赋值给ColumnInfo，再增加到SyncModifyTableInfo中
        for(int i=0;i<addColumns.size();i++) {
            HBaseColumnInfo hBaseColumnInfo = new HBaseColumnInfo();
            hBaseColumnInfo.setComment(addColumns.get(i).getColumnDesc());
            hBaseColumnInfo.setHiveType( SyncCheckUtil.parseColumnToHiveType(mm, addColumns.get(i)) );
            hBaseColumnInfo.setColName(addColumns.get(i).getName());
            hBaseColumnInfo.setFamilyName(addColumns.get(i).getColumnFamily());
            addColumn[i] = hBaseColumnInfo;
            add.setType(SyncModifyTableInfo.SYNC_ADD);
        }
        add.setColumns(addColumn);

        SyncModifyTableInfo modify = new SyncModifyTableInfo();
        modify.setDbType(srcInfo.getType().name());
        modify.setDatabase(srcInfo.getName());
        modify.setTable(info.getJob_media_name());
        HBaseColumnInfo[] modifyColumn = new HBaseColumnInfo[addColumns.size()];
        for(int i=0;i<modifyColumns.size();i++) {
            HBaseColumnInfo hBaseColumnInfo = new HBaseColumnInfo();
            hBaseColumnInfo.setComment(modifyColumns.get(i).getColumnDesc());
            hBaseColumnInfo.setHiveType( SyncCheckUtil.parseColumnToHiveType(mm, modifyColumns.get(i)) );
            hBaseColumnInfo.setColName(modifyColumns.get(i).getName());
            hBaseColumnInfo.setFamilyName(modifyColumns.get(i).getColumnFamily());
            modifyColumn[i] = hBaseColumnInfo;
            modify.setType(SyncModifyTableInfo.SYNC_MODIFY);
        }
        modify.setColumns(modifyColumn);
    }
}
