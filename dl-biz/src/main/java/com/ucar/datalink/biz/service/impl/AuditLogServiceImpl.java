package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.AuditLogDAO;
import com.ucar.datalink.biz.service.AuditLogService;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogDAO auditLogDAO;

    @Override
    public Integer insert(AuditLogInfo auditLogInfo) {
        return auditLogDAO.insert(auditLogInfo);
    }

    @Override
    public List<AuditLogInfo> getListByParam(AuditLogInfo param) {
        return auditLogDAO.getListByParam(param);
    }

}
