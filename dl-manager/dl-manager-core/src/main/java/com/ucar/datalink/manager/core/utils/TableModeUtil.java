package com.ucar.datalink.manager.core.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.media.ModeUtils;
import org.apache.oro.text.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 匹配分表，并转化成如t_dl_test_[0000-0031]格式的通配符
 *
 * @author wenbin.song
 * @date 2019/04/03
 */
public class TableModeUtil {
    private static final Logger logger = LoggerFactory.getLogger(TableModeUtil.class);
    private static final String MODE_PATTERN = "(.*)_(\\d{1,4})$";

    private static LoadingCache<String, Pattern> patterns = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {
        @Override
        public Pattern load(String key) throws Exception {
            PatternCompiler pc = new Perl5Compiler();
            try {
                return pc.compile(key,
                        Perl5Compiler.CASE_INSENSITIVE_MASK
                                | Perl5Compiler.READ_ONLY_MASK);
            } catch (MalformedPatternException e) {
                throw new DatalinkException("invalid config", e);
            }
        }
    });

    public static void doTableModel(List<String> tableList)  {
        Map<String,TreeSet<String>> tableSetMap = new HashMap<>(32);
        PatternMatcher matcher = new Perl5Matcher();
        try {
            for (String table:tableList) {
                if( isWildTable(matcher,table)){
                    if(ModeUtils.isMonthlySuffix(table)) {
                        continue;
                    }
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
                            tableList.add(prefixStr+"_["+startStr +"-"+endStr+"]");
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
                tableList.add(prefixStr+"_["+startStr +"-"+endStr+"]");
            }
        } catch (Exception e) {
            logger.info("处理分表时报错:",e);
        }
    }

    public static boolean isWildTable(PatternMatcher matcher, String table) throws ExecutionException {
        return matcher.matches(table, patterns.get(MODE_PATTERN));
    }

}
