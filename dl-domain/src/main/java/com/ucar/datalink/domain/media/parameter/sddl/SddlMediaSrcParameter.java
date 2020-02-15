package com.ucar.datalink.domain.media.parameter.sddl;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lubiao on 2017/3/20.
 */
public class SddlMediaSrcParameter extends MediaSrcParameter {
    private List<Long> primaryDbsId = new ArrayList<>();
    private List<Long> secondaryDbsId = new ArrayList<>();
    private Long proxyDbId;

    public void setProxyDbId(Long proxyDbId) {
        this.proxyDbId = proxyDbId;
    }

    public Long getProxyDbId() {
        return proxyDbId;
    }

    public List<Long> getPrimaryDbsId() {
        return primaryDbsId;
    }

    public void setPrimaryDbsId(List<Long> primaryDbsId) {
        this.primaryDbsId = primaryDbsId;
    }

    public List<Long> getSecondaryDbsId() {
        return secondaryDbsId;
    }

    public void setSecondaryDbsId(List<Long> secondaryDbsId) {
        this.secondaryDbsId = secondaryDbsId;
    }

}
