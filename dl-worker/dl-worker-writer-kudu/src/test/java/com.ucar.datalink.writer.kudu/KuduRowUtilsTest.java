package com.ucar.datalink.writer.kudu;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.writer.kudu.util.KuduRowUtils;
import com.ucar.datalink.writer.kudu.util.KuduUtils;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.ColumnTypeAttributes;
import org.apache.kudu.Schema;
import org.apache.kudu.client.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/14
 */
public class KuduRowUtilsTest {

    public static void main(String[] args) {
        test();
    }


    public static void test(){

            ArrayList<String> ms = new ArrayList<>();
            ms.add("10.104.132.72:7051");
            ms.add("10.104.132.73:7051");
            ms.add("10.104.132.75:7051");
            ms.add("10.104.132.223:7051");
            ms.add("10.104.132.221:7051");

            KuduClient client = KuduUtils.createClient(ms);
            KuduTable kuduTable = null;
            try {
                kuduTable = client.openTable("impala::kudu_test.t_v_model");
                Upsert upsert = kuduTable.newUpsert();
                PartialRow row = upsert.getRow();

                List<ColumnSchema> columns = kuduTable.getSchema().getColumns();

                ColumnSchema idColumnSchema = null;
                ColumnSchema priceColumnSchema = null;
                for(ColumnSchema cs : columns){
                    if(cs.getName().equals("id")){
                        idColumnSchema = cs;
                    }
                    if(cs.getName().equals("price")){
                        priceColumnSchema = cs;
                    }
                }

                long id = 1L;
                KuduRowUtils.setValue(row,id,idColumnSchema);
                String price = "5.88999999999";
                KuduRowUtils.setValue(row,price,priceColumnSchema);
                KuduSession kuduSession = client.newSession();
                kuduSession.apply(upsert);
                kuduSession.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                try {
                    client.close();
                } catch (KuduException e) {
                    e.printStackTrace();
                }
            }
    }



}
