package com.ucar.datalink.biz.utils.flinker.module;

import com.ucar.datalink.domain.job.JobConfigInfo;

/**
 * Created by user on 2017/9/27.
 */
public class TimingJobExtendPorperty {

    private String isOpen = "false";

    private String type = JobConfigInfo.TIMING_TRANSFER_TYPE_FULL;

    private String workAddress;

    private String parameter;

    public boolean isOpen() {
        if("true".equalsIgnoreCase(isOpen)) {
            return true;
        }
        return false;
    }

    public void setIsOpen(String isOpen) {
        this.isOpen = isOpen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
