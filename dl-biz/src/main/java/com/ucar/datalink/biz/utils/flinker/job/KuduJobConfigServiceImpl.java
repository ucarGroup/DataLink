package com.ucar.datalink.biz.utils.flinker.job;

import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.module.JobExtendProperty;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KuduJobConfigServiceImpl extends AbstractJobConfigService{

    private static final Logger LOGGER = LoggerFactory.getLogger(KuduJobConfigServiceImpl.class);

    @Override
    public String createReaderJson(MediaSourceInfo info, List<ColumnMeta> metas, JobExtendProperty property, String mediaName) {
        return null;
    }


    private void checkType(MediaSrcParameter parameter) {
        if( !(parameter instanceof KuduMediaSrcParameter)) {
            throw new RuntimeException("media source type error "+parameter);
        }
    }

    @Override
    public String createWriterJson(MediaSourceInfo srcInfo, MediaSourceInfo info, MediaMeta srcMediaMeta, JobExtendProperty property, String mediaName) {
        checkType(info.getParameterObj());
        KuduMediaSrcParameter parameter = (KuduMediaSrcParameter)info.getParameterObj();
        List<KuduMediaSrcParameter.KuduMasterConfig> kuduMasterConfigs = parameter.getKuduMasterConfigs();

        String masterDddresses;
        String database = parameter.getDatabase();
        List<String> columnNames = new ArrayList<String>();

        List<ColumnMeta> column = srcMediaMeta.getColumn();
        for(ColumnMeta columnMeta :column){
            columnNames.add(columnMeta.getName());
        }

        ArrayList<String> mdList = new ArrayList<>();
        for (KuduMediaSrcParameter.KuduMasterConfig k :kuduMasterConfigs){
            mdList.add(String.format("\"%s:%d\"", k.getHost(),k.getPort()));
        }
        masterDddresses = ArrayUtils.toString(mdList);
        masterDddresses = masterDddresses.substring(2,masterDddresses.length() - 2);

        String json = "";
        try{
            MediaMeta target = changeNameToAlias( MetaMapping.transformToRDBMS(srcMediaMeta) );
            String columns = buildColumnParm( target.getColumn() );
            columns = columns.substring(1,columns.length() - 1);
            String database2Table = String.format("%s.%s",database,mediaName);

            String writer = loadJobConfig(FlinkerJobConfigConstant.KUDU_WRITER);
            writer = writer.replace(FlinkerJobConfigConstant.KUDU_MASTER_ADDRESSES,masterDddresses);
            writer = writer.replace(FlinkerJobConfigConstant.KUDU_TABLE,database2Table);
            writer = writer.replace(FlinkerJobConfigConstant.KUDU_COLUMN,columns);
            json = writer;

        }catch (Exception e){
            LOGGER.error("kudu createWriterJson error ",e);
        }
        return json;
    }


    /**
     *
     * @param list
     * @return
     */
    @Override
    public String buildColumnParm(List<ColumnMeta> list){
        StringBuffer buf = new StringBuffer();
        if(list!=null && list.size() > 0){
            for(int i=0;i<list.size();i++){
                String fieldName = list.get(i).getName().toLowerCase();
                if(i==list.size()-1){
                    buf.append("\"").append(fieldName).append("\"");
                }else{
                    buf.append("\"").append(fieldName).append("\"").append(",");
                }
            }
        }
        return buf.toString();
    }






}
