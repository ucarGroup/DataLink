package com.ucar.datalink.biz.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/7/4.
 * 获取ES相关的元信息工具类，ES获取的元信息直接用HTTP方式请求服务端就可以获取了，但是返回的JSON格式不好用对象去匹配，只能一点一点的解析
 */
public class ElasticSearchUtil {


    /**
     * 根据传入的MediaSourceInfo 获取所有表的元信息
     * @param info
     * @return
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) {
        checkElasticSearch(info);
        String json = executeGetTables(info.getParameterObj());
        return parseTypesInfo(json);
    }


    /**
     * 根据传入的MediaSourceInfo和表名，获取这个表下的所有列的元信息
     * @param info
     * @param tableName
     * @return
     */
    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) {
        checkElasticSearch(info);
        String[] names = tableName.split("\\.");
        if(names==null || names.length<2) {
            throw new RuntimeException("elastic search table error");
        }
        String index_name = names[0];
        String type_name = names[1];
        String json = executeGetColumns(info.getParameterObj(),index_name,type_name);
        return parseFieldInfo(json);
    }


    /**
     * 根据传入的json，解析出 List<MediaMeta>
     * 这个函数和 parseFieldInfo 需要解析的结构都是很类似的，所以就以这个函数为列来说明
     * 之所以要搞这么复杂的循环里面再套循环的解析，是因为每次拿到的JSON格式很多字段都是动态变化的，不好对应到一个具体的java对象，
     * 只能一点点的嵌套解析
     *  下面这个json中  fcar_asset_car_300是index的名字，这个值可能每次都不一样，将这个index的名字作为Map.Entry的key，那么value就是剩下的那么一大堆
     *  部分了，然后再做一个循环再解析 ,第二层解析的时候只解析fcar_asset_car_300里面的内容，解析出的依旧是key-value的形式
     *  第二层循环解析的key就是 mappings 这个字符串，这个值是固定的，value就是mappings里面嵌套的那一堆内容了，
     *  所以第二层循环解析的是第一层的value部分，将第一层的value部分再解析成key-value的形式，
     *  第三层解析的是第二层的value部分，将第二层的value部分再解析成key-value的形式，一直解析到最里面的 "type" : "long"
     *
     *  之后再做地三层循环解析，Map.Entry的key就是 t_asset_car_brand，value就是t_asset_car_brand里面嵌套的部分，而这个t_asset_car_brand
     *  就是type的名字，所以到这里就可以确定一个表的名字了，就是index_name.type_name 也就是fcar_asset_car_300.t_asset_car_brand
     *  继续第四层循环 key就是properties 这个key是固定的，value就是里面嵌套的部分，properties里面就是types的字段，也就是表的字段了
     *  第五层循环中需要解析的就是字段的名字，注意有些字段可能是复杂类型，比如name_suggest里面又嵌套了一层properties，这种复杂类型就忽略
     *  解析，而 t_asset_car_brand|id 这个值就是一个字段的名字
     *  第六层循环就是解析像 t_asset_car_brand|id里面的内容，这个key可能是type，也可能是其他类型，我们只需要字段类型所以其他类型忽略，
     *  只要key是 type这个字符串就可以了，对应的value就是type的类型比如long，string等
     *  整个多层循环结束后，List<MediaMeta>就可以解析出来了

     {
        "fcar_asset_car_300":{
            "mappings":{
                "t_asset_car_brand":{
                    "properties":{
                        "name_suggest":{
                            "properties":{
                                "input":{
                                    "type":"string"
                                }
                            }
                        },
                        "t_asset_car_brand|id":{
                            "type":"long"
                        },
                        "t_asset_car_brand|initial":{
                            "type":"string",
                            "index":"not_analyzed"
                        },
                    }
                }
            }
        }
     }
     *
     * @param json
     */
    public static List<MediaMeta> parseTypesInfo(String json) {
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {
        });
        List<MediaMeta> tables = new ArrayList<>();
        for (Map.Entry<String, String> index : jsonMap.entrySet()) {
            //这里拿到的是index的名字，我们已经知道index名字了，所以继续遍历
            //MediaMeta tm = new MediaMeta();
            //tm.setNameSpace(index.getKey());

            LinkedHashMap<String, String> mapping = JSON.parseObject(index.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String, String> mapping_content : mapping.entrySet()) {
                //这里拿到的e.getKey() 是mappings，这个值不需要继续遍历

                LinkedHashMap<String, String> types_info = JSON.parseObject(mapping_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                });
                for (Map.Entry<String, String> type_content : types_info.entrySet()) {
                    //这里拿到的key就是type的名字，value是type的所有属性，继续遍历getValue()获取type的属性
                    //ES情况特殊，一个表的名字是 indez名字和type的名字组合  index_name.type_name
                    MediaMeta tm = new MediaMeta();
                    tm.setNameSpace(index.getKey());
                    tm.setName( type_content.getKey() );
                    tm.setDbType(MediaSourceType.ELASTICSEARCH);
                    List<ColumnMeta> columns = new ArrayList<>();

                    LinkedHashMap<String, String> properties = JSON.parseObject(type_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                    });
                    for (Map.Entry<String, String> prop : properties.entrySet()) {
                        //这里的getKey()就是 properties，遍历value获取  {"author":{"type":"string"},"title":{"type":"string"},"content":{"type":"string"}}
                        //这里可能会包含 dynamic 类型，忽略即可
                        if( !prop.getKey().equals("properties") ) {
                            continue;
                        }
                        LinkedHashMap<String, String> fields_info = JSON.parseObject(prop.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                        });
                        for (Map.Entry<String, String> field_content : fields_info.entrySet()) {
                            //这里拿到的type_content.getKey()就是一个字段的名字，如上面的author，title
                            //value就是字段的类型，类型中可能会出现复杂类型，忽略这些复杂类型，只解析普通类型
                            ColumnMeta cm = new ColumnMeta();
                            String field_name = field_content.getKey();
                            cm.setName(field_name);

                            LinkedHashMap<String, String> field_types = JSON.parseObject(field_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                            });
                            for (Map.Entry<String, String> field__type_info : field_types.entrySet()) {
                                //System.out.println("field info : "+field__type_info.getKey()+"  : "+field__type_info.getValue());
                                //这里解析出的 key-value可能是 "type" : "long" 这样的
                                //也可能是一个内嵌类型(名字是properties)，还有可能是 "index" : "not_analyzed"
                                //或者是  "format" : "strict_date_optional_time||epoch_millis"
                                //这里只需要解析type就可以了，type对应的value就是这个字段的类型，其他都忽略就可以
                                //将这个类型赋给 ColumnMeta，就完成了一个字段类型的解析
                                if ("type".equals(field__type_info.getKey())) {
                                    cm.setType(field__type_info.getValue());
                                    break;
                                }
                            }
                            //如果是复杂列或者不认识的列，则type内容为空，这时忽略这个字段，不加入到List<Column>中
                            if(cm.getType()!=null) {
                                columns.add(cm);
                            }
                        }
                    }
                    tm.setColumn(columns);
                    tables.add(tm);
                }
            }
        }//完成json遍历
        return tables;

    }


    /**
     * 根据传入的json，解析出 List<MediaMeta>
     * @param json
     */
    public static List<ColumnMeta> parseFieldInfo(String json) {
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {
        });
        List<ColumnMeta> columns = new ArrayList<>();

        for (Map.Entry<String, String> index : jsonMap.entrySet()) {
            //这里拿到的是index的名字，我们已经知道index名字了，所以继续遍历

            LinkedHashMap<String, String> mapping = JSON.parseObject(index.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String, String> mapping_content : mapping.entrySet()) {
                //这里拿到的e.getKey() 是mappings，这个值不需要继续遍历

                LinkedHashMap<String, String> types_info = JSON.parseObject(mapping_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                });
                for (Map.Entry<String, String> type_content : types_info.entrySet()) {
                    //这里拿到的key就是type的名字，value是type的所有属性，继续遍历getValue()获取type的属性

                    LinkedHashMap<String, String> properties = JSON.parseObject(type_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                    });
                    for (Map.Entry<String, String> prop : properties.entrySet()) {
                        //这里的getKey()就是 properties，遍历value获取  {"author":{"type":"string"},"title":{"type":"string"},"content":{"type":"string"}}
                        //如果不是 properties字段则忽略
                        if( !prop.getKey().equals("properties") ) {
                            continue;
                        }
                        LinkedHashMap<String, String> fields_info = JSON.parseObject(prop.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                        });
                        for (Map.Entry<String, String> field_content : fields_info.entrySet()) {
                            //这里拿到的type_content.getKey()就是一个字段的名字，如上面的author，title
                            //value就是字段的类型，类型中可能会出现复杂类型，忽略这些复杂类型，只解析普通类型
                            ColumnMeta cm = new ColumnMeta();
                            String field_name = field_content.getKey();
                            cm.setName(field_name);

                            LinkedHashMap<String, String> field_types = JSON.parseObject(field_content.getValue(), new TypeReference<LinkedHashMap<String, String>>() {
                            });
                            for (Map.Entry<String, String> field__type_info : field_types.entrySet()) {
                                //System.out.println("field info : "+field__type_info.getKey()+"  : "+field__type_info.getValue());
                                //这里解析出的 key-value可能是 "type" : "long" 这样的
                                //也可能是一个内嵌类型(名字是properties)，还有可能是 "index" : "not_analyzed"
                                //或者是  "format" : "strict_date_optional_time||epoch_millis"
                                //这里只需要解析type就可以了，type对应的value就是这个字段的类型，其他都忽略就可以
                                //将这个类型赋给 ColumnMeta，就完成了一个字段类型的解析
                                if ("type".equals(field__type_info.getKey())) {
                                    cm.setType(field__type_info.getValue());
                                    break;
                                }
                            }
                            //如果是复杂列或者不认识的列，则type内容为空，这时忽略这个字段，不加入到List<Column>中
                            if(cm.getType()!=null) {
                                columns.add(cm);
                            }
                        }
                    }
                }
            }
        }//完成json遍历
        return columns;
    }






    /**
     * 执行一个HTTP请求获取元数据信息
     * @param parameter
     * @return
     */
    private static String executeGetTables(EsMediaSrcParameter parameter) {
        String[] arr = parameter.getClusterHosts().split(",");
        for(String ip : arr) {
            String url = "http://"+ ip +":"+ parameter.getHttpPort()  +"/_mapping?pretty";
            String name = parameter.getUserName();
            String password = parameter.getPassword();
            String result = URLConnectionUtil.retryGETWithAuth(url,name,password);
            return result;
        }
        return "";
    }


    /**
     * 执行一个HTTP请求获取元数据信息，获取某个表下的所有列信息
     * @param parameter
     * @return
     */
    private static String executeGetColumns(EsMediaSrcParameter parameter, String index_name, String type_name) {
        String[] arr = parameter.getClusterHosts().split(",");
        for(String ip : arr) {
            String url = "http://"+ip +":"+ parameter.getHttpPort() +"/"+index_name+"/"+ type_name +"/_mapping?pretty";
            String name = parameter.getUserName();
            String password = parameter.getPassword();
            String result = URLConnectionUtil.retryGETWithAuth(url,name,password);
            return result;
        }
        return "";
    }


    /**
     * 检查当前的MediaSourceInfo所包含的类似是否是ElasticSearch的
     * @param info
     */
    private static void checkElasticSearch(MediaSourceInfo info) {
        MediaSrcParameter parameter = info.getParameterObj();
        if( !(parameter instanceof EsMediaSrcParameter) ) {
            throw new RuntimeException("当前的MediaSrcParameter类型错误 "+parameter);
        }
    }


}
