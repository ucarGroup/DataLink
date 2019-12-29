package com.ucar.datalink.writer.es.util;

import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.ModeUtils;
import com.ucar.datalink.domain.plugin.writer.es.EsMappingParameter;
import com.ucar.datalink.writer.es.handle.Constants;
import org.apache.commons.lang.StringUtils;

/**
 * Created by lubiao on 2019/12/28.
 */
public class EsPrefixUtil {

    /**
     * 根据mapping配置情况获取前缀信息
     */
    public static String getEsPrefixName(String tableName, MediaMappingInfo mappingInfo) {
        if (mappingInfo.isEsUsePrefix()) {
            EsMappingParameter parameter = mappingInfo.getParameterObj();
            if (parameter != null && StringUtils.isNotBlank(parameter.getPrefixName())) {
                return parameter.getPrefixName() + Constants.SEPARATOR;
            } else {
                //tableName如果是按月分表，把月份后缀去掉
                String result1 = ModeUtils.tryBuildMonthlyPattern(tableName);
                if (ModeUtils.isMonthlyPattern(result1)) {
                    tableName = tableName.substring(0, tableName.length() - 7);
                    return tableName + Constants.SEPARATOR;
                }

                //tableName如果是按年份分表，把年份后缀去掉
                String result2 = ModeUtils.tryBuildYearlyPattern(tableName);
                if (ModeUtils.isYearlyPattern(result2)) {
                    tableName = tableName.substring(0, tableName.length() - 5);
                    return tableName + Constants.SEPARATOR;
                }

                //tableName如果符合分表表达式的规则，则分表序号去掉
                MediaInfo.ModeValue mode = ModeUtils.parseMode(tableName);
                if (mode.getMode().isMulti()) {
                    tableName = tableName.substring(0, tableName.length() - 5);
                    return tableName + Constants.SEPARATOR;
                }

                return tableName + Constants.SEPARATOR;
            }
        }
        return "";
    }
}
