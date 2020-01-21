package com.ucar.datalink.domain.plugin.writer.sddl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 27/10/2017.
 */
public class SddlWriterParameter extends PluginWriterParameter {
    private final static String TABLE_SPACE_MARK = "#";

    @Override
    public String initPluginName() {
        return "writer-sddl";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.sddl.SddlTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.sddl.SddlTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.SDDL);
    }

    private String missMatchSkipTables; // 用于对非法表同步，进行提示预警
    private List<String> missMatchSkipTableList;

    public String getMissMatchSkipTables() {
        return missMatchSkipTables;
    }

    public void setMissMatchSkipTables(String missMatchSkipTables) {
        this.missMatchSkipTables = missMatchSkipTables;
    }

    public List<String> getMissMatchSkipTableList() {
        if (StringUtils.isNotEmpty(getMissMatchSkipTables())) {
            return Lists.newArrayList(getMissMatchSkipTables().split(TABLE_SPACE_MARK));
        }
        return missMatchSkipTableList;
    }

}
