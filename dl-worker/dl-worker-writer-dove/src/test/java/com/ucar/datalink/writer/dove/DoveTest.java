package com.ucar.datalink.writer.dove;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.writer.dove.vo.DBEventType;
import com.ucar.datalink.writer.dove.vo.DBTableRowCellVO;
import com.ucar.datalink.writer.dove.vo.DBTableRowVO;
import com.ucarinc.dove.common.message.Message;
import com.ucarinc.framework.dove.DoveConstants;
import com.ucarinc.framework.dove.producer.ProducerManager;

import java.util.Arrays;
import java.util.List;

/**
 * @auther yifan.liu02
 * @date 2019/11/14
 */
public class DoveTest {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("project.name", "datalink");
        System.setProperty(DoveConstants.SPRING_DOVE_META_SERVER, "10.104.105.5:5181,10.104.108.91:5181,10.104.108.92:5181,10.104.114.14:5181,10.104.114.15:5181,10.104.114.16:5181");
        DBTableRowVO vo = new DBTableRowVO();
        vo.setDatabaseName("db1");
        vo.setTableName("t1");
        vo.setEventType(DBEventType.INSERT);
        vo.setId("1");
        DBTableRowCellVO rowCellVO = new DBTableRowCellVO();
        rowCellVO.setColumnName("column1");
        rowCellVO.setBeforeValue("b1");
        rowCellVO.setAfterValue("a1");
        List<DBTableRowCellVO> list = Arrays.asList(rowCellVO);
        vo.setDbTableRowCellVOList(list);
        ProducerManager.send(new Message<Object, Object>("dove_test_liuyifan", "k1", JSON.toJSONString(vo)));

    }
}
