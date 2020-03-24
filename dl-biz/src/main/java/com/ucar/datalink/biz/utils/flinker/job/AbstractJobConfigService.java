package com.ucar.datalink.biz.utils.flinker.job;

import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.job.HostNodeInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by user on 2017/7/19.
 */
public abstract class AbstractJobConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobConfigService.class);

    public abstract String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName);

    public abstract String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, MediaMeta writerMeta, JobExtendProperty property, String destName);

    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, List<String> names) {
        return "";
    }

    public MediaSourceInfo getMediaSourceInfoById(long id) {
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        return mediaSourceService.getById(id);
    }


    public String merge(String readerJobConfig,String writerJobConfig){
        try{
            DLConfig jobConfigReader = DLConfig.parseFrom(readerJobConfig);
            DLConfig jobConfigWriter = DLConfig.parseFrom(writerJobConfig);
            DLConfig config = jobConfigReader.merge(jobConfigWriter,false);
            return config.toJSON();
        }catch (Exception e){
            LOGGER.error("createJobConfig is error",e);
        }
        return null;
    }

    /**
     * 加载jobconfig文件
     * @param fileName
     * @return
     */
    public String loadJobConfig(String fileName){
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        StringBuffer buf = new StringBuffer();
        try {
            while((line = br.readLine()) != null) {
                buf.append(line);
            }
        } catch (IOException e) {
            LOGGER.error("loadJobConfig is exception fileName:"+fileName,e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    LOGGER.error("loadJobConfig reader close exception fileName:" + fileName, e1);
                }
            }
        }
        return buf.toString();
    }


    public String buildColumnParm(List<ColumnMeta> list){
        StringBuffer buf = new StringBuffer();
        if(list!=null && list.size() > 0){
            for(int i=0;i<list.size();i++){
                if(i==list.size()-1){
                    buf.append("\"").append(list.get(i).getName()).append("\"");
                }else{
                    buf.append("\"").append(list.get(i).getName()).append("\"").append(",");
                }
            }
        }
        return buf.toString();
    }

    public String readerMopUp(String json, MediaSourceInfo info) {
        return json;
    }

    public String writerMopUp(String json, MediaSourceInfo info, JobExtendProperty property) {
        return json;
    }

    public String parseMediaName(String name) {
        if(StringUtils.isNotBlank(name)) {
            String[] names = name.split("\\.");
            if(names.length == 2) {
                return names[0];
            }
        }
        return name;
    }

    public String reloadReader(String json,MediaSourceInfo info) {
        return json;
    }

    public String reloadWriter(String json, MediaSourceInfo info) {
        return json;
    }

    public String replaceJsonResult(String json, Object obj, MediaSourceInfo srcInfo) {
        return json;
    }


    public String replaceColumns(String content, String replaceStr) {
        try {
            return content.replaceAll(FlinkerJobConfigConstant.COLUMN, replaceStr);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(),e);
            return content.replaceAll(FlinkerJobConfigConstant.COLUMN, Matcher.quoteReplacement(replaceStr));
        }
    }


    public MediaMeta changeNameToAlias(MediaMeta meta) {
        List<ColumnMeta> columnMetas = meta.getColumn();
        columnMetas.forEach(c -> {
            if(StringUtils.isNotBlank(c.getAliasName())) {
                c.setName(c.getAliasName());
            }
        });
        return meta;
    }

    public List<HostNodeInfo> parseSrcContent(String json) {
        throw new UnsupportedOperationException();
    }

    public List<HostNodeInfo> parseDestContent(String json) {
        throw new UnsupportedOperationException();
    }

    public List<HostNodeInfo> parseSrcMediaSourceToHostNode(MediaSourceInfo info) {
        throw new UnsupportedOperationException();
    }

    public List<HostNodeInfo> parseDestMediaSourceToHostNode(MediaSourceInfo info) {
        throw new UnsupportedOperationException();
    }

    public boolean compareSrcHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return true;
    }


    public boolean compareDestHostInfos(List<HostNodeInfo> fromJobConfigHosts, List<HostNodeInfo> fromMediaSourceHosts) {
        return true;
    }

    public String parseHostInfo(String json, String path) {
        DLConfig connConf = DLConfig.parseFrom(json);
        try {
            Object obj = connConf.get(path);
            if(obj instanceof String) {
                return (String)obj;
            }
            if(obj instanceof Integer) {
                return ((Integer)obj).toString();
            }
            if(obj instanceof Long) {
                return ((Long)obj).toString();
            }
            if(obj instanceof Boolean) {
                return ((Boolean)obj).toString().toLowerCase();
            }
            if(obj instanceof Float) {
                return ((Float)obj).toString();
            }
            if(obj instanceof Double) {
                return ((Double)obj).toString();
            }
        }catch(Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return "";
    }

    public List<String> parseHostInfoToList(String json, String path) {
        DLConfig connConf = DLConfig.parseFrom(json);
        List<String> list = new ArrayList<>();
        try {
            Object obj = connConf.get(path);
            if(obj instanceof String) {
                String str = (String)obj;
                list.add(str);
            }
            if(obj instanceof List) {
                List<String> li = (List<String>)obj;
                list.addAll(li);
            }
        }catch(Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    public void prepare(JobExtendProperty property) {

    }



}
