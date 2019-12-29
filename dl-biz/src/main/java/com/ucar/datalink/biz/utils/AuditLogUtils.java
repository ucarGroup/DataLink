package com.ucar.datalink.biz.utils;

import com.ucar.datalink.biz.service.AuditLogService;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;

import java.util.Date;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
public class AuditLogUtils {
    private static AuditLogService logService;

    public static Integer saveAuditLog(AuditLogInfo info){
        if(logService==null){
            logService = DataLinkFactory.getObject(AuditLogService.class);
        }
        info.setOperTime(new Date());
        return logService.insert(info);
    }
}
