package com.ucar.datalink.writer.kudu;

import com.ucar.datalink.writer.kudu.util.KuduUtils;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.ColumnTypeAttributes;
import org.apache.kudu.Schema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xy.li
 * @date 2019/05/14
 */
public class KuduTableDescriptionTest {

    public static void main(String[] args) {
        test();
    }

    public static void test(){

        try {
            ArrayList<String> ms = new ArrayList<>();
            ms.add("10.104.132.72:7051");
            ms.add("10.104.132.73:7051");
            ms.add("10.104.132.75:7051");
            ms.add("10.104.132.223:7051");
            ms.add("10.104.132.221:7051");

            KuduClient client = KuduUtils.createClient(ms);
            KuduTable  kuduTable = client.openTable("impala::rtl_ods_uc.t_v_model");
            Schema schema = kuduTable.getSchema();
            List<ColumnSchema> columns = schema.getColumns();
            for(ColumnSchema cs : columns){
                System.out.println(cs.toString());
            }

            for(ColumnSchema cs : columns){
                ColumnTypeAttributes typeAttributes = cs.getTypeAttributes();
                if (typeAttributes != null) {
                    System.out.println(typeAttributes.toString());
                }
            }
        } catch (KuduException e) {
            e.printStackTrace();
        }

    }



    public static void test2(){
        double i = 3.856;

        // 舍掉小数取整
        System.out.println("舍掉小数取整:Math.floor(3.856)=" + (int) Math.floor(i));

        // 四舍五入取整
        System.out.println("四舍五入取整:(3.856)="
                + new BigDecimal(i).setScale(0, BigDecimal.ROUND_HALF_UP));

        // 四舍五入保留两位小数
        System.out.println("四舍五入取整:(3.856)="
                + new BigDecimal(i).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 凑整，取上限
        System.out.println("凑整:Math.ceil(3.856)=" + (int) Math.ceil(i));

        // 舍掉小数取整
        System.out.println("舍掉小数取整:Math.floor(-3.856)=" + (int) Math.floor(-i));
        // 四舍五入取整
        System.out.println("四舍五入取整:(-3.856)="
                + new BigDecimal(-i).setScale(0, BigDecimal.ROUND_HALF_UP));

        // 四舍五入保留两位小数
        System.out.println("四舍五入取整:(-3.856)="
                + new BigDecimal(-i).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 凑整，取上限
        System.out.println("凑整(-3.856)=" + (int) Math.ceil(-i));
    }




}
