package com.ucar.datalink.manager.core.web.controller.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import com.ucar.datalink.domain.vo.RdbmsOperatorVO;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by user on 2018/4/2.
 */

@Controller
@RequestMapping(value = "/rdbms/")
@LoginIgnore
public class RdbmsServiceController {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsServiceController.class);

    private static final String MYSQL = MediaSourceType.MYSQL.name();

    private static final String SQLSERVER = MediaSourceType.SQLSERVER.name();

    /**
     * 对表的count操作的rest接口
     */
    private static final String GET_COUNT = "/rdbms/count";

    /**
     * http调用默认等待20秒超时
     */
    private static final long DEFAULT_WAIT_TIME_BY_SECOND = 20;

    @Autowired
    WorkerService service;

    @Autowired
    MediaService mediaService;



    @RequestMapping(value = "/count")
    @ResponseBody
    public Object count(@RequestParam("DB_TYPE") String type, @RequestParam("DB_NAME") String name, @RequestParam("SQL") String sql) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleJsonArray("-1","db type empty");
            }
            if ( !sqlSyntaxCheck(sql) ) {
                return assembleJsonArray("-1","sql syntax check failure");
            }

            List<MediaSourceInfo> list = null;
            if( MYSQL.equalsIgnoreCase(type) ) {
                //执行mysql语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                //执行sqlserver语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else {
                //不支持，抛错
                return assembleJsonArray("-1","unsupport db type");
            }

            boolean isFindDBName = false;
            MediaSourceInfo mediaSourceInfo = null;
            for(MediaSourceInfo info : list) {
                if( info.getName().equalsIgnoreCase(name) ) {
                    mediaSourceInfo = info;
                    isFindDBName = true;
                    break;
                }
            }
            if( !isFindDBName ) {
                //传入的db name在数据库中没找到，抛错
                return assembleJsonArray("-1","not found db name");
            }

            long id = mediaSourceInfo.getId();
            //将 media_source_info的id， 通过rest接口传给worker，worker执行完后再返回

            //String url =
            //URLConnectionUtil.retryPOST()
            RdbmsOperatorVO vo = new RdbmsOperatorVO();
            vo.setMediaSourceId(id);
            vo.setSql(sql);
            String result = execute(vo,GET_COUNT);
            return assembleJsonArray(result,"SUCCESS");
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleJsonArray("-1",e.getMessage());
        }
    }


    @RequestMapping(value = "/tables")
    @ResponseBody
    public Object tables(@RequestParam("DB_TYPE") String type, @RequestParam("DB_NAME") String name ) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleJsonArrayWithTables(new String[]{}, "db type empty");
            }

            List<MediaSourceInfo> list = null;
            if( MYSQL.equalsIgnoreCase(type) ) {
                //执行mysql语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                //执行sqlserver语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else {
                //不支持，抛错
                return assembleJsonArrayWithTables(new String[]{}, "unsupport db type");
            }
            boolean isFindDBName = false;
            MediaSourceInfo mediaSourceInfo = null;
            for(MediaSourceInfo info : list) {
                if( info.getName().equalsIgnoreCase(name) ) {
                    mediaSourceInfo = info;
                    isFindDBName = true;
                    break;
                }
            }
            if( !isFindDBName ) {
                //传入的db name在数据库中没找到，抛错
                return assembleJsonArrayWithTables(new String[]{}, "not found db name");
            }

            List<MediaMeta> metas = null;
            if(mediaSourceInfo.getType()==MediaSourceType.MYSQL || mediaSourceInfo.getType()==MediaSourceType.SQLSERVER) {
                metas = RDBMSUtil.getTables(mediaSourceInfo);
            }
            else {
                //不支持
                throw new UnsupportedOperationException("db type unsupport "+mediaSourceInfo.getType());
            }

            logger.debug(metas.toString());
            String[] tableNames = null;
            if(metas!=null && metas.size()>0) {
                tableNames = new String[metas.size()];
                for(int i=0;i< metas.size();i++) {
                    tableNames[i] = metas.get(i).getName();
                }
            }
            else {
                tableNames = new String[0];
            }
            return assembleJsonArrayWithTables(tableNames, "SUCCESS");
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleJsonArrayWithTables(new String[]{}, e.getMessage());
        }
    }



    @RequestMapping(value = "/showTable")
    @ResponseBody
    public Object showTable(@RequestParam("DB_TYPE") String type, @RequestParam("DB_NAME") String name, @RequestParam("TABLE_NAME") String tableName ) {
        try {
            if(StringUtils.isBlank(type) ) {
                return assembleJsonArrayWithTableInfo(new TableInfo[]{}, "db type empty");
            }

            List<MediaSourceInfo> list = null;
            if( MYSQL.equalsIgnoreCase(type) ) {
                //执行mysql语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL);
            }
            else if( SQLSERVER.equalsIgnoreCase(type) ) {
                //执行sqlserver语句
                list = mediaService.getMediaSourcesByTypes(MediaSourceType.SQLSERVER);
            }
            else {
                //不支持，抛错
                return assembleJsonArrayWithTableInfo(new TableInfo[]{}, "unsupport db type");
            }
            boolean isFindDBName = false;
            MediaSourceInfo mediaSourceInfo = null;
            for(MediaSourceInfo info : list) {
                if( info.getName().equalsIgnoreCase(name) ) {
                    mediaSourceInfo = info;
                    isFindDBName = true;
                    break;
                }
            }
            if( !isFindDBName ) {
                //传入的db name在数据库中没找到，抛错
                return assembleJsonArrayWithTableInfo(new TableInfo[]{}, "not found db name");
            }

            List<ColumnMeta> columns = null;
            if(mediaSourceInfo.getType()==MediaSourceType.MYSQL || mediaSourceInfo.getType()==MediaSourceType.SQLSERVER) {
                columns = RDBMSUtil.getColumns(mediaSourceInfo, tableName);
            }
            else {
                //不支持
                throw new UnsupportedOperationException("db type unsupport "+mediaSourceInfo.getType());
            }
            logger.debug(columns.toString());
            TableInfo[] infos = null;
            if(columns!=null && columns.size()>0) {
                infos = new TableInfo[columns.size()];
                for(int i=0;i<columns.size();i++) {
                    ColumnMeta cm = columns.get(i);
                    TableInfo ti = new TableInfo();
                    ti.setColumnDesc(cm.getColumnDesc());
                    ti.setDecimalDigits(cm.getDecimalDigits());
                    ti.setLength(cm.getLength());
                    ti.setName(cm.getName());
                    ti.setType(cm.getType());
                    infos[i] = ti;
                }
            }
            else {
                infos = new TableInfo[0];
            }
            return assembleJsonArrayWithTableInfo(infos, "SUCCESS");
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return assembleJsonArrayWithTableInfo(new TableInfo[]{}, e.getMessage());
        }
    }




    public static Object assembleJsonArrayWithTableInfo(TableInfo[] infos, String msg) {
        ResponseInfo info = new ResponseInfo();
        info.setInfos(infos);
        info.setCount(infos.length+"");
        info.setMsg(msg);
        String json = JSONObject.toJSONString(info);
        Object obj = JSONObject.parse(json);
        return obj;
    }


    public static Object assembleJsonArrayWithTables(String[] tables, String msg) {
        ResponseInfo info = new ResponseInfo();
        info.setTables(tables);
        info.setMsg(msg);
        info.setCount(tables.length+"");
        String json = JSONObject.toJSONString(info);
        Object obj = JSONObject.parse(json);
        return obj;
    }


    public static Object assembleJsonArray(String count, String msg) {
        ResponseInfo info = new ResponseInfo();
        info.setCount(count);
        info.setMsg(msg);
        String json = JSONObject.toJSONString(info);
        Object obj = JSONObject.parse(json);
        return obj;
    }

    public String asyncExecute(RdbmsOperatorVO vo ,String path) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newCachedThreadPool();
        AsyncExecuteThread thread = new AsyncExecuteThread(vo,path);
        FutureTask<String> task = new FutureTask<String>(thread);
        Future<String> future = (Future<String>)executor.submit(task);
        String result = future.get(DEFAULT_WAIT_TIME_BY_SECOND,TimeUnit.SECONDS);
        return result;
    }

    //加一个带超时的异步类
    private class AsyncExecuteThread implements Callable<String> {
        final RdbmsOperatorVO vo;
        final String path;
        AsyncExecuteThread(RdbmsOperatorVO vo,String path) {
            this.vo = vo;
            this.path = path;
        }

        @Override
        public String call() throws Exception {
            return execute(vo,path);
        }
    }

    /**
     * 调用rest接口并返回结果
     * @param vo
     * @param path
     * @return
     */
    private String execute(RdbmsOperatorVO vo ,String path) {
        String json = JSONObject.toJSONString(vo);
        String address = getWorkAddress();
        //address = "localhost:8083" ;//for debug
        String url = "http://"+ address + path;
        String result = URLConnectionUtil.retryPOST(url,json);
        return result;
    }

    /**
     * 随机选取一个worker机器的ip
     * @return
     */
    private String getWorkAddress() {
        List<WorkerInfo> list = service.getList();
        Random rand = new Random();
        if(list==null || list.size()==0) {
            throw new RuntimeException("list is emtpy");
        }
        int index = Math.abs( rand.nextInt(list.size()) );
        WorkerInfo info = list.get(index);
        String address = info.getWorkerAddress();
        Integer port = info.getRestPort();
        String address_and_port = address +":"+ port;
        return address_and_port;
    }




    /**
     * 传入的sql中部能包含 update，delete，truncate等操作
     * 只能是 select count 这样的操作
     * @param sql
     * @return
     */
    private static boolean sqlSyntaxCheck(String sql) {
        //如果包含 update或者 delete等，就返回false
        if( StringUtils.isBlank(sql) ) {
            return false;
        }
        String upperCase = sql.toUpperCase().trim();
        if( upperCase.startsWith("UPDATE") || upperCase.startsWith("DELETE") || upperCase.startsWith("TRUNCATE") ) {
            return false;
        }
        if( upperCase.matches("SELECT +COUNT.+") ) {
            return true;
        }
        return false;
    }



    public static void main(String[] args) {
        RdbmsServiceController r = new RdbmsServiceController();
        r.go();
    }

    public void go() {
        String sql = "select    count(id) from xx;";
        sqlSyntaxCheck(sql);

        TableInfo[] ti = new TableInfo[]{};
        ti = new TableInfo[2];
        TableInfo t1 = new TableInfo();
        t1.setName("aaa");
        TableInfo t2 = new TableInfo();
        t2.setName("bbb");
        ti[0] = t1;
        ti[1] = t2;
        Object x = assembleJsonArrayWithTableInfo(ti,"hehe");
        System.out.println(x);
    }


    private static class ResponseInfo {
        private String count;
        private String msg;
        private String[] tables;
        private TableInfo[] infos;

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String[] getTables() {
            return tables;
        }

        public void setTables(String[] tables) {
            this.tables = tables;
        }

        public TableInfo[] getInfos() {
            return infos;
        }

        public void setInfos(TableInfo[] infos) {
            this.infos = infos;
        }
    }


    private class TableInfo {
        //列明
        private String name;
        //列类型
        private String type;
        //类长度
        private Integer length;
        //列信息
        private String columnDesc;
        //列精度
        private Integer decimalDigits;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getColumnDesc() {
            return columnDesc;
        }

        public void setColumnDesc(String columnDesc) {
            this.columnDesc = columnDesc;
        }

        public Integer getDecimalDigits() {
            return decimalDigits;
        }

        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }
    }

}
