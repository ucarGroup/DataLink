package com.ucar.datalink.biz.utils.flinker.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.ModeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class JobContentParseUtil {

    private static Logger logger = LoggerFactory.getLogger(JobContentParseUtil.class);

    private static final String MYSQL_READER = "mysqlreader";


    public static String parseJobReaderType(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            JSONObject obj = (JSONObject) connConf.get("job.content[0].reader");
            String name = (String)obj.get("name");
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseJobWriterType(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            JSONObject obj = (JSONObject)connConf.get("job.content[0].writer");
            String name = (String)obj.get("name");
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseReaderDBMSTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.connection[0].table");
            String name = parseDBMSTables(obj);
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }


    public static String parseWriterDBMSTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter.connection[0].table");
            String name = parseDBMSTables(obj);
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }


    private static String parseDBMSTables(Object obj) {
        String name = "";
        if(obj instanceof List) {
            List<String> list = (List<String>)obj;
            if(list.size()>1) {
                name = list.get(0);
                int size = list.size();
                if(name.length()>4) {
                    //截取表的最后4位，然后看这四位是否都是数字，如果是则表示这是一个分表的任务
                    //将表最后四位改成 [0000-size] 这个格式
                    //size是分了多少个表，可能是32个，可能是64，也可能是128...
                    //这里默认用4位，也就是默认分的表不超过9999个
                    String end_string = name.substring(name.length()-4);
                    char[] cs = end_string.toCharArray();
                    if(Character.isDigit(cs[0]) && Character.isDigit(cs[1]) && Character.isDigit(cs[2]) && Character.isDigit(cs[3])) {
                        String tmp = ""+size;
                        while(tmp.length()<=4) {
                            tmp = "0"+tmp;
                        }
                        end_string = "[0000-"+tmp+"]";
                    }
                    name = name.substring(0,name.length()-4);
                    name = name + end_string;
                } else {
                    name = list.get(0);
                }
            }else if(list.size()==1){
                name = list.get(0);
            }

        }
        if(obj instanceof String) {
            name = (String)obj;
        }
        return name;
    }

    public static String parseReaderHBaseTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.table");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseWriterHBaseTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter.table");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseReaderESTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.esIndex");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            obj = connConf.get("job.content[0].reader.parameter.esType");
            if(obj instanceof String) {
                name = name + "."+(String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseWriterESTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter.esIndex");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            obj = connConf.get("job.content[0].writer.parameter.esType");
            if(obj instanceof String) {
                name = name +"."+ (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }



    public static String parseReaderKuduTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.table");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseWriterKuduTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter.table");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseReaderHDFSTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].reader.parameter.fileName");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }

    public static String parseWriterHDFSTable(String json) {
        try {
            DLConfig connConf = DLConfig.parseFrom(json);
            Object obj = connConf.get("job.content[0].writer.parameter.fileName");
            String name = "";
            if(obj instanceof String) {
                name = (String)obj;
            }
            return name;
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
        return "";
    }


    public static String getReaderTable(String json) {
        String type = parseJobReaderType(json);
        String name = "";
        switch (type){
            case "mysqlreader":
            case "sqlserverreader":
            case "hanareader":
            case "oraclereader":
            case "postgresqlreader":
                name = parseReaderDBMSTable(json);
                break;
            case "hbasereader98":
                name = parseReaderHBaseTable(json);
                break;
            case "hdfsreader":
                name = parseReaderHDFSTable(json);
                break;
            case "esreader":
                name = parseReaderESTable(json);
                break;
        }
        return name;
    }

    public static String getWriterTable(String json) {
        String type = parseJobWriterType(json);
        String name = "";
        switch (type){
            case "mysqlwriter":
            case "sqlserverwriter":
            case "postgresqlwriter":
                name = parseWriterDBMSTable(json);
                break;
            case "hbasewriter98":
                name = parseWriterHBaseTable(json);
                break;
            case "hdfswriter":
                name = parseWriterHDFSTable(json);
                break;
            case "eswriter":
                name = parseWriterESTable(json);
                break;
        }
        return name;
    }

    public static boolean isMySQLReader(String json) {
        String reader_name = getReaderTable(json);
        return MYSQL_READER.equals(reader_name);
    }

    public static List<String> parseMutilTables(String table_name) {
        MediaInfo.ModeValue modeValue = ModeUtils.parseMode(table_name);
        if(modeValue.getMode().isMulti()) {
            return modeValue.getMultiValue();
        }
        List<String> list = new ArrayList<String>();
        list.add(table_name);
        return list;
    }

    public static boolean isMutilTables(String table_name) {
        MediaInfo.ModeValue modeValue = ModeUtils.parseMode(table_name);
        return modeValue.getMode().isMulti();
    }

    public static String getMutilTableFirt(String table_name) {
        MediaInfo.ModeValue modeValue = ModeUtils.parseMode(table_name);
        return modeValue.getMultiValue().get(0);
    }

}
