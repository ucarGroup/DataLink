package com.ucar.datalink.flinker.core.admin.rest;

import com.ucar.datalink.flinker.core.admin.Command;

/**
 * Created by user on 2017/7/11.
 */
public class ExtendCommand extends Command {

    private String job_content;


    public String getJob_content() {
        return job_content;
    }

    public void setJob_content(String job_content) {
        this.job_content = job_content;
    }
}
