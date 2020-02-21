package com.ucar.datalink.flinker.plugin.writer.kuduwriter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ucar.datalink.flinker.api.util.ErrorRecord;;
import java.util.*;

public class KuduUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(KuduUtils.class);

    public static boolean isExistTable(List<String> masterAddresses, String tableName){
        if(StringUtils.isEmpty(tableName) || "".equals(tableName.trim())){
            return false;
        }
        KuduClient client = createClient(masterAddresses);
        try {
            boolean tableExists = client.tableExists(tableName);
            return tableExists;
        } catch (KuduException e) {
            LOG.info(e.getMessage());
        } finally {
            closeClient(client);
        }
        return false;
    }

    public static MetaTable getMetaTable(List<String> masterAddresses, String tableName, String[] columns) throws Exception {
        KuduClient client = null;
        try {
            client = createClient(masterAddresses);
            KuduTable kuduTable = client.openTable(tableName);
            Schema schema = kuduTable.getSchema();
            List<ColumnSchema> columnSchemas = schema.getColumns();
            Set<String> realColumnNames = new HashSet<String>();
            for (ColumnSchema cs : columnSchemas) {
                realColumnNames.add(cs.getName());
            }
            Set<String> inputColumnNames = new HashSet<String>(Arrays.asList(columns));
            LOG.info("config column:"  + ArrayUtils.toString(inputColumnNames.toString()));
            LOG.info("kudu column:"  + ArrayUtils.toString(realColumnNames));
            inputColumnNames.removeAll(realColumnNames);

            //如果kudu不包含输入字段,抛出异常
            if (inputColumnNames.size() != 0) {
            	ErrorRecord.addError(String.format("字段配置与kudu实际不相符{%s}!",ArrayUtils.toString(inputColumnNames.toString())));
                throw new Exception(String.format("字段配置与kudu实际不相符{%s}!",ArrayUtils.toString(inputColumnNames.toString())));
            }

            return new MetaTable(columnSchemas);
        } catch (KuduException e) {
            throw e;
        } finally {
            closeClient(client);
        }
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
        master.add("10.104.132.72:7051");
        master.add("10.104.132.73:7051");
        master.add("10.104.132.75:7051");
        master.add("10.104.132.223:7051");
        master.add("10.104.132.221:7051");
        KuduClient client = createClient(master);
        try {
            List<String> tablesList = client.getTablesList().getTablesList();
            System.out.println(ArrayUtils.toString(tablesList));
        } catch (KuduException e) {
            e.printStackTrace();
        } finally {
            closeClient(client);
        }


    }


}
