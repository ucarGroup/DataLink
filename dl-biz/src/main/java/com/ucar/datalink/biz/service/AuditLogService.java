package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.auditLog.AuditLogInfo;

import java.util.List;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
public interface AuditLogService {
    Integer insert(AuditLogInfo auditLogInfo);
    List<AuditLogInfo> getListByParam(AuditLogInfo param);
}
