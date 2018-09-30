package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.mail.MailInfo;

/**
 * Created by lubiao on 2018/3/22.
 */
public interface MailService {

    void sendMail(MailInfo mailInfo);
}
