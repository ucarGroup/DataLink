package com.ucar.datalink.domain.plugin.writer.dummy;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lubiao on 2017/2/17.
 */
public class DummyWriterParameter extends PluginWriterParameter {

    @Override
    public String initPluginName() {
        return "writer-dummy";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.dummy.DummyTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.dummy.DummyTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet();
    }

    public static void main(String args[]) {
        List<DummyWriterParameter> list = new ArrayList<>();
        list.add(new DummyWriterParameter());
        System.out.println(JSONObject.toJSONString(list, SerializerFeature.WriteClassName));
    }
}
