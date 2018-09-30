package com.ucar.datalink.domain.plugin.reader.dummy;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginParameter;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;

import java.util.Set;

/**
 * Created by lubiao on 2017/2/17.
 */
public class DummyReaderParameter extends PluginReaderParameter {

    @Override
    public String initPluginName() {
        return "reader-dummy";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.reader.dummy.DummyTaskReader";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.reader.dummy.DummyTaskReaderListener";
    }
    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet();
    }

    public static void main(String args[]) {
        String str = new DummyReaderParameter().toJsonString();
        System.out.println(str);
        PluginParameter parameter = JSONObject.parseObject(str, PluginParameter.class);
        System.out.println(parameter.getClass().getCanonicalName());
    }
}
