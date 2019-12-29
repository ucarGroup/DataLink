package com.ucar.datalink.manager.core.web.dto.taskDecorate;

import com.ucar.datalink.domain.decorate.TaskDecorateDetail;
import org.apache.commons.beanutils.BeanUtils;

import java.text.SimpleDateFormat;

/**
 * @author xy.li
 * @date 2019/06/10
 */
public class TaskDecorateDetailView extends TaskDecorateDetail {

    public void fillProperty(TaskDecorateDetail taskDecorateDetail) {
        try {
            BeanUtils.copyProperties(this, taskDecorateDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCreateTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.format(this.getCreateTime());
    }

    public String getUpdateTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.format(this.getUpdateTime());
    }


}
