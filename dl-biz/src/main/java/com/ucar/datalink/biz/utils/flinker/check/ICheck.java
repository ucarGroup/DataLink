package com.ucar.datalink.biz.utils.flinker.check;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;

/**
 * Created by yang.wang09 on 2018-05-17 15:46.
 */
public interface ICheck {


    void check(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, JobConfigInfo info) throws Exception;


}
