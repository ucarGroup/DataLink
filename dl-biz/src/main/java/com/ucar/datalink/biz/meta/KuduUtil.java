package com.ucar.datalink.biz.meta;


import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * created by swb on 2019/01/04
 */
public class KuduUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(KuduUtil.class);

    /**
     * 根据传入的MediaSourceInfo 获取所有表的元信息
     * @param info
     * @return
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) throws KuduException {
        checkKudu(info);

        KuduMediaSrcParameter parameter = info.getParameterObj();
        List<String> hostList = parameter.getKuduMasterConfigs().stream().
                map(config->{return MessageFormat.format("{0}:{1}",config.getHost(),String.valueOf(config.getPort()));}).
                collect(Collectors.toList());

        KuduClient client = createClient(hostList);
       try {
           return execute(client);
       }catch (KuduException e){
            LOGGER.info("执行kudu查询时报错:",e);
            throw new RuntimeException(e);
       } finally{
           closeClient(client);
       }
    }

    private static List<MediaMeta> execute(KuduClient client) throws KuduException {
        List<String> tables = client.getTablesList().getTablesList();
        List<MediaMeta> list = Lists.newArrayList();
        for (String tableName:tables){
            KuduTable kuduTable = client.openTable(tableName);
            list.add(parseKuduTable(kuduTable));
        }
        return list;
    }

    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) {
        checkKudu(info);

        KuduMediaSrcParameter parameter = info.getParameterObj();
        List<String> hostList = parameter.getKuduMasterConfigs().stream().
                map(config->{return MessageFormat.format("{0}:{1}",config.getHost(),String.valueOf(config.getPort()));}).
                collect(Collectors.toList());

        KuduClient client = createClient(hostList);
        try {
            KuduTable kuduTable = client.openTable(tableName);
            return getColumnMetaList(kuduTable);
        } catch (KuduException e){
            LOGGER.info("执行kudu查询时报错:",e);
            throw new RuntimeException(e);
        } finally{
            closeClient(client);
        }
    }

    private static MediaMeta parseKuduTable(KuduTable kuduTable) {
        MediaMeta mediaMeta = new MediaMeta();
        List<ColumnMeta> columnMetaList = getColumnMetaList(kuduTable);

        String name = kuduTable.getName();
        String[] names = name.split("\\.");
        if(names.length>1) {
            mediaMeta.setNameSpace(name.split("\\.")[0]);
            mediaMeta.setName(name.split("\\.")[1]);
        }else {
            mediaMeta.setNameSpace(name.split("\\.")[0]);
            mediaMeta.setName(name.split("\\.")[0]);
        }

        mediaMeta.setDbType(MediaSourceType.KUDU);
        mediaMeta.setColumn(columnMetaList);
        return mediaMeta;
    }

    private static void checkKudu(MediaSourceInfo info) {
        MediaSrcParameter parameter = info.getParameterObj();
        if( !(parameter instanceof KuduMediaSrcParameter) ) {
            throw new RuntimeException("当前的MediaSrcParameter类型错误 "+parameter);
        }
    }


    public static List<String> checkTargetTables(MediaSourceInfo realTargetMediaSourceInfo, Set<String> tableNameSet) throws KuduException {
        List<String> returnList = Lists.newArrayList();
        List<MediaMeta> mediaMetaList = getTables(realTargetMediaSourceInfo);
        List<String> tableNameList = mediaMetaList.stream().map(m->{ return m.getName();}).collect(Collectors.toList());
        Iterator<String> it = tableNameSet .iterator();
        while (it.hasNext()){
            String targetTableName = it.next();
            if(!tableNameList.contains(targetTableName)){
                returnList.add(targetTableName);
            }
        }
        return returnList;
    }

    public static boolean isExistTable(List<String> masterAddresses, String tableName){
        if(StringUtils.isEmpty(tableName) || "".equals(tableName.trim())){
            return false;
        }
        KuduClient client = createClient(masterAddresses);
        try {
            boolean tableExists = client.tableExists(tableName);
            return tableExists;
        } catch (KuduException e) {
            LOGGER.info(e.getMessage());
        } finally {
            closeClient(client);
        }
        return false;
    }

    public static KuduClient createClient(List<String> masterAddresses) {
        try{
            KuduClient build = new KuduClient.KuduClientBuilder(masterAddresses).build();
            return build;
        }catch(Exception e){
            throw new RuntimeException("链接异常!");
        }
    }

    public static void closeClient(KuduClient client){
        if(client != null){
            try {
                client.close();
            } catch (KuduException e) {
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<String> master = new ArrayList<String>();
        master.add("10.104.132.75:7051");
        master.add("10.104.132.73:7051");
        master.add("10.104.132.72:7051");
        master.add("10.104.132.221:7051");
        master.add("10.104.132.223:7051");
        KuduClient client = createClient(master);
        try {
            List<MediaMeta> list = execute(client);
            for(MediaMeta mediaMeta:list){
                System.out.println("tableName:"+mediaMeta.getName());
                List<ColumnMeta>  columnMetaList = mediaMeta.getColumn();
                for (ColumnMeta columnMeta:columnMetaList){
                    System.out.println(columnMeta.getName());
                }
            }

        }catch (KuduException e){
            LOGGER.info("执行kudu查询时报错:",e);
            throw new RuntimeException(e);
        } finally{
            closeClient(client);
        }
/*        KuduClient client = createClient(master);
        try {
            List<String> tablesList = client.getTablesList().getTablesList();
            System.out.println("------????------");
            System.out.println(ArrayUtils.toString(tablesList));
            KuduTable kuduTable = client.openTable(tablesList.get(0));
            System.out.println(kuduTable.getName().split("\\.")[1]+" : "+kuduTable.getName());
            System.out.println("------end------");
        } catch (KuduException e) {
            e.printStackTrace();
        } finally {
            closeClient(client);
        }*/
    }


    public static List<ColumnMeta> getColumnMetaList(KuduTable kuduTable) {
        List<ColumnMeta> columnMetaList = Lists.newArrayList();
        List<ColumnSchema> columnSchemas = kuduTable.getSchema().getColumns();

        columnSchemas.forEach(columnSchema -> {
            ColumnMeta columnMeta = new ColumnMeta();
            columnMeta.setType(columnSchema.getType().getName());
            columnMeta.setIsPrimaryKey(columnSchema.isKey());
            columnMeta.setName(columnSchema.getName());
            columnMeta.setLength(columnSchema.getType().getSize(columnSchema.getTypeAttributes()));
            columnMetaList.add(columnMeta);
        });
        return columnMetaList;
    }
}
