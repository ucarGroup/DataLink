package com.ucar.datalink.manager.core.web.util;

import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.task.TaskInfo;

/**
 * @author yifan.liu02
 * @date 2018/12/29
 */
public class AuditLogInfoUtil {
    public static AuditLogInfo getAuditLogInfoFromMediaSourceInfo(MediaSourceInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperType(operType);
        if(info!=null){
            logInfo.setOperName(info.getName());
            logInfo.setOperKey(info.getId());
            logInfo.setOperRecord(info.toString());
        }
        return logInfo;
    }
    public static AuditLogInfo getAuditLogInfoFromTaskInfo(TaskInfo t, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperType(operType);
        if(t!=null){
            logInfo.setOperName(t.getTaskName());
            logInfo.setOperKey(t.getId());
            logInfo.setOperRecord(t.toString());
        }
        return logInfo;
    }
}
