package com.ucar.datalink.manager.core.sharding;

import org.apache.oro.text.regex.*;
import org.junit.Test;

import java.util.*;

/**
 * 测试分表通配符
 *
 * @author wenbin.song
 * @date 2019/03/27
 */
public class ShardingTableTest {

    private static final String reg = "(.*)_(\\d+)$";

    @Test
    public void checkShardingTable() throws MalformedPatternException {

        PatternCompiler pc = new Perl5Compiler();

        PatternMatcher matcher = new Perl5Matcher();

        Map<String,TreeSet<String>> tableSetMap = new HashMap<>();

        String b = "t_dl_test_0002";
        String a = "t_dl_test_0001";
        String e = "t_dl_goods_201910";
        String d = "t_dl_test_0004";
        String c = "t_dl_test_0003";
        String k = "b_ty_d";
        String f = "t_dl_goods_201909";
        String g = "t_dl_test_0000";
        String h = "t_dl_goods_201908";
        String t = "t_dl_user_0001";
        String w = "t_dl_user_0002";
        String r = "t_dl_user_0003";
        String x = "t_dl_user_0004";
        String t1 = "t_dl_test_0011";
        String t2 = "t_dl_test_0012";
        String t3 = "t_dl_test_0013";
        String t4 = "t_dl_test_0014";
        String t5 = "t_dl_test_0008";
        String t6 = "t_dl_test_0006";
        String t7 = "t_dl_test_r0007";

        List<String> tableList = new ArrayList<>();
        tableList.add(b);
        tableList.add(a);
        tableList.add(e);
        tableList.add(d);
        tableList.add(c);
        tableList.add(f);
        tableList.add(g);
        tableList.add(h);
        tableList.add(k);
        tableList.add(t);
        tableList.add(w);
        tableList.add(r);
        tableList.add(x);
        tableList.add(t1);
        tableList.add(t2);
        tableList.add(t3);
        tableList.add(t4);
        tableList.add(t5);
        tableList.add(t6);
        tableList.add(t7);

        for (String table:tableList) {
           if( matcher.matches(table,pc.compile(reg,Perl5Compiler.CASE_INSENSITIVE_MASK
                   | Perl5Compiler.READ_ONLY_MASK))){
               MatchResult matchResult = matcher.getMatch();
               String prefix = matchResult.group(1);
               TreeSet<String> treeSet =  tableSetMap.get(prefix);
               if(treeSet == null) {
                   treeSet = new TreeSet<>();
               }
               treeSet.add(table);
               tableSetMap.put(prefix,treeSet);
           }
        }

        for (Map.Entry<String,TreeSet<String>> entry : tableSetMap.entrySet()) {
            String startStr = "";
            String endStr = "";
            String prefixStr = entry.getKey();
            TreeSet<String> treeSet = entry.getValue();
            if(treeSet.size() < 4) {
                continue;
            }
            Iterator<String> it =  treeSet.iterator();
            int i = 0;
            while (it.hasNext()){
                String table = it.next();
                String currentStr = table.substring(prefixStr.length()+1);
                if(i != 0 && (Integer.valueOf(currentStr) - Integer.valueOf(endStr)) != 1) {
                    if(i != 0 && i != 1){
                        System.out.println(prefixStr+"_["+startStr +"-"+endStr+"]");
                    }
                    i = 0;
                    continue;
                }
                endStr = currentStr;
                if(i == 0) {
                    startStr = endStr;
                }
                i++;
            }
            System.out.println(prefixStr+"_["+startStr +"-"+endStr+"]");
        }
    }

}
