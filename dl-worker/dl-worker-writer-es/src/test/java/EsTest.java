import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.BatchContentVo;
import com.ucar.datalink.writer.es.client.rest.vo.BatchDocVo;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class EsTest {

    public static void main(String[] args) throws UnsupportedEncodingException {
        BatchDocVo route = new BatchDocVo("es-ucar-test01");
        route.setBatchType("_bulk");

        int i = 1;

        for (;;){
            List<BatchContentVo> list = new ArrayList<BatchContentVo>();
            Map<String,Object> map = new HashMap<>();
            map.put("name","eang"+i);
            map.put("sex",1);
            map.put("age",4);
            map.put("create_time",new Date());
            BatchContentVo contentVo = new BatchContentVo();
            contentVo.setRetainNullValue(true);
            contentVo.setIndex("user_test");
            contentVo.setType("user");
            contentVo.setId(i+"");
            contentVo.setBatchActionEnum(ESEnum.BatchActionEnum.UPSERT);
            contentVo.setContent(map);
            list.add(contentVo);
            EsClient.batchDocWithResultParse(route,list);
            i++;
        }





    }
}
