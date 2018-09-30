package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.meta.MetaMappingInfo;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by user on 2017/11/3.
 */
public interface MetaMappingDAO {

    public List<MetaMappingInfo> selectAllMetaMapping();

    public MetaMappingInfo selectMetaMappingById(long id);

    public void addMetaMapping(MetaMappingInfo info);

    public void updateMetaMapping(MetaMappingInfo info);

    public void deleteMetaMapping(long id);

    public List<MetaMappingInfo> selectAllMetaMappingByType(@Param("srcMediaSourceType")String srcMediaSourceType, @Param("targetMediaSourceType")String targetMediaSourceType);

}
