package com.ucar.datalink.domain.sync;

import java.util.List;

/**
 * Created by sqq on 2017/9/27.
 */
public class SyncApplyContent {

    List<SyncApplyParameter> applyParameterList;

    public List<SyncApplyParameter> getApplyParameterList() {
        return applyParameterList;
    }

    public void setApplyParameterList(List<SyncApplyParameter> applyParameterList) {
        this.applyParameterList = applyParameterList;
    }
}
