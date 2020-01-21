package com.ucar.datalink.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.SyncApplyService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.domain.sync.SyncApproveInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang.wang09 on 2018-04-24 15:26.
 */
public class MailUtil {

    private static Logger logger = LoggerFactory.getLogger(MailUtil.class);

    private static final LoadingCache<LoadingKey, Boolean> applyMailed;

    private static UserService userService;

    private static MailService mailService;

    private static SyncApplyService syncApplyService;

    static {
        userService = DataLinkFactory.getObject(UserService.class);
        mailService = DataLinkFactory.getObject(MailService.class);
        syncApplyService = DataLinkFactory.getObject(SyncApplyService.class);

        applyMailed = CacheBuilder.newBuilder().build(new CacheLoader<LoadingKey, Boolean>() {
            @Override
            public Boolean load(LoadingKey key) throws Exception {
                return true;
            }
        });
    }

    public static void sendMailByAsynchronous(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names, List<String> paths, SyncApplyInfo applyInfo) {
        new Thread(new SendMailThread(srcInfo, destInfo, names, paths, applyInfo), "send_job_task_by_mail").start();
    }


    private static class SendMailThread implements Runnable {
        private MediaSourceInfo srcInfo;
        private MediaSourceInfo destInfo;
        private String names;
        private List<String> paths;
        private SyncApplyInfo applyInfo;

        public SendMailThread(MediaSourceInfo srcInfo, MediaSourceInfo destInfo, String names, List<String> paths, SyncApplyInfo applyInfo) {
            this.srcInfo = srcInfo;
            this.destInfo = destInfo;
            this.names = names;
            if (paths != null && paths.size() > 0) {
                this.paths = new ArrayList<String>(paths);
            } else {
                this.paths = new ArrayList<String>();
            }
            this.applyInfo = applyInfo;
        }

        public void run() {
            try {
                sendMail(srcInfo, destInfo, names, paths, applyInfo);
            } catch (Exception e) {
                logger.error("send mail failure", e);
            }
        }
    }


    private static void sendMail(MediaSourceInfo src, MediaSourceInfo dest, String names, List<String> paths, SyncApplyInfo applyInfo) throws Exception {
        String title = src.getName() + "库同步" + dest.getType().name();
        String content = DataxUtil.assembleMailInfo(src, dest, names, paths);
        MailInfo info = new MailInfo();
        info.setSubject(title);
        info.setMailContent(content);
        List<String> recipient = new ArrayList<>();
        List<UserInfo> users = userService.getUserInfoByReceiveMail();
        UserInfo applyUser = applyInfo.getApplyUserInfo();
        users.add(applyUser);
        if (users != null && users.size() > 0) {
            for (UserInfo u : users) {
                if (StringUtils.isNotBlank(u.getUcarEmail())) {
                    String email = u.getUcarEmail() + "@ucarinc.com";
                    recipient.add(email);
                    String luckyEmail = u.getUcarEmail() + "@luckincoffee.com";
                    recipient.add(luckyEmail);
                }
            }
        }
        info.setRecipient(recipient);
        mailService.sendMail(info);
    }


    public static void sendEmail(SyncApplyInfo applyInfo) {
        try {
            MailInfo mailInfo = new MailInfo();
            List<String> recipient = new ArrayList<>();
            String subject = "";
            String content = "";
            String env = ConfigReadUtil.getString("datax.env");
            SyncApplyStatus applyStatus = applyInfo.getApplyStatus();
            if (applyStatus == SyncApplyStatus.REJECTED) {
                List<SyncApproveInfo> rejectedApproveList = syncApplyService.getRejectedApproveByApplyId(applyInfo.getId());
                String rejectRemark = "";
                for (SyncApproveInfo approveInfo : rejectedApproveList) {
                    String remark = approveInfo.getApproveRemark();
                    rejectRemark = rejectRemark + remark + "<br>" + "<br>";
                }
                subject = "同步申请ID = " + applyInfo.getId() + " 被拒绝！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请被拒绝，ID = " + applyInfo.getId() + "<br>" + "<br>" + "拒绝理由：" + rejectRemark;
            } else if (applyStatus == SyncApplyStatus.SUCCEEDED) {
                String replyRemark = applyInfo.getReplyRemark();
                subject = "同步申请ID = " + applyInfo.getId() + " 执行成功！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请执行成功，ID = " + applyInfo.getId() + "<br>" + "<br>" + "备注：" + replyRemark;
            } else if (applyStatus == SyncApplyStatus.FAILED) {
                String replyRemark = applyInfo.getReplyRemark();
                subject = "同步申请ID = " + applyInfo.getId() + " 执行失败！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请执行失败，ID = " + applyInfo.getId() + "<br>" + "<br>" + "备注：" + replyRemark;
                List<UserInfo> superUserList = userService.getUserInfoByRoleTypeAndIsAlarm(RoleType.SUPER);
                if (superUserList != null && superUserList.size() > 0) {
                    for (UserInfo superUser : superUserList) {
                        if (StringUtils.isNotBlank(superUser.getUcarEmail())) {
                            String ucarEmail = superUser.getUcarEmail() + "@ucarinc.com";
                            recipient.add(ucarEmail);
                        }
                    }
                }
            } else if (applyStatus == SyncApplyStatus.FULL_EXECUTING) {
                subject = "同步申请ID = " + applyInfo.getId() + " 请前往datalink平台执行全量job！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请ID = " + applyInfo.getId() + ", 状态为FULL_EXECUTING，请执行该申请ID的全量job！（所在位置：全量任务-Job配置管理-根据申请ID筛选Job）";
                UserInfo applyUser = userService.getById(applyInfo.getApplyUserId());
                if (applyUser != null) {
                    if (StringUtils.isNotBlank(applyUser.getUcarEmail())) {
                        String ucarEmail = applyUser.getUcarEmail() + "@ucarinc.com";
                        recipient.add(ucarEmail);
                        String luckyEmail = applyUser.getUcarEmail() + "@luckincoffee.com";
                        recipient.add(luckyEmail);
                    }
                }
            } else if (applyStatus == SyncApplyStatus.FULL_FINISH) {
                String replyRemark = applyInfo.getReplyRemark();
                subject = "同步申请ID = " + applyInfo.getId() + " 全量job执行成功，请确认是否需要增量，并通知数据同步答疑协助执行！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请ID = " + applyInfo.getId() + ", 全量job执行成功，状态为FULL_FINISH，请确认是否需要增量，并通知管理员执行！" + "<br>" + "<br>" + "备注：" + replyRemark;
                UserInfo applyUser = userService.getById(applyInfo.getApplyUserId());
                if (applyUser != null) {
                    if (StringUtils.isNotBlank(applyUser.getUcarEmail())) {
                        String ucarEmail = applyUser.getUcarEmail() + "@ucarinc.com";
                        recipient.add(ucarEmail);
                        String luckyEmail = applyUser.getUcarEmail() + "@luckincoffee.com";
                        recipient.add(luckyEmail);
                    }
                }
            } else if (applyStatus == SyncApplyStatus.FULL_FAILED) {
                String replyRemark = applyInfo.getReplyRemark();
                subject = "同步申请ID = " + applyInfo.getId() + " 请重新执行失败的全量job!";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请ID = " + applyInfo.getId() + ", 状态为FULL_FAILED，请重新执行失败的全量job！（所在位置：全量任务-Job配置管理-根据申请ID筛选Job）" + "<br>" + "<br>" + "备注：" + replyRemark;
                UserInfo applyUser = userService.getById(applyInfo.getApplyUserId());
                if (applyUser != null) {
                    if (StringUtils.isNotBlank(applyUser.getUcarEmail())) {
                        String ucarEmail = applyUser.getUcarEmail() + "@ucarinc.com";
                        recipient.add(ucarEmail);
                        String luckyEmail = applyUser.getUcarEmail() + "@luckincoffee.com";
                        recipient.add(luckyEmail);
                    }
                }
            } else if (applyStatus == SyncApplyStatus.INCREMENT_EXECUTING) {
                subject = "同步申请ID = " + applyInfo.getId() + " 请前往配置增量映射！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请ID = " + applyInfo.getId() + ", 状态为INCREMENT_EXECUTING，请配置该申请ID的增量映射！";
                List<UserInfo> superUserList = userService.getUserInfoByRoleTypeAndIsAlarm(RoleType.SUPER);
                if (superUserList != null && superUserList.size() > 0) {
                    for (UserInfo executeUser : superUserList) {
                        if (StringUtils.isNotBlank(executeUser.getUcarEmail())) {
                            String ucarEmail = executeUser.getUcarEmail() + "@ucarinc.com";
                            recipient.add(ucarEmail);
                        }
                    }
                }
            } else if (applyStatus == SyncApplyStatus.INCREMENT_FAILED) {
                String replyRemark = applyInfo.getReplyRemark();
                subject = "同步申请ID = " + applyInfo.getId() + " 增量映射生成失败，请进行手动配置！";
                content = "当前环境：" + env + "<br>" + "<br>" + "同步申请ID = " + applyInfo.getId() + ", 状态为INCREMENT_FAILED，增量配置失败，请前往配置增量映射！" + "<br>" + "<br>" + "备注：" + replyRemark;
                List<UserInfo> superUserList = userService.getUserInfoByRoleTypeAndIsAlarm(RoleType.SUPER);
                if (superUserList != null && superUserList.size() > 0) {
                    for (UserInfo executeUser : superUserList) {
                        if (StringUtils.isNotBlank(executeUser.getUcarEmail())) {
                            String ucarEmail = executeUser.getUcarEmail() + "@ucarinc.com";
                            recipient.add(ucarEmail);
                        }
                    }
                }
            } else if (applyStatus == SyncApplyStatus.SUBMITTED) {
                subject = "同步申请ID = " + applyInfo.getId() + " 待您审批！请前往datalink平台进行审批！";
                content = "当前环境：" + env + "<br>" + "<br>" + "请审批同步申请，ID = " + applyInfo.getId() + "<br>" + "<br>" + "生产环境登陆地址：http://datalinkmanager.10101111.com/";
                List<SyncApproveInfo> approveInfos = syncApplyService.getSyncApproveInfoByApplyId(applyInfo.getId());
                for (SyncApproveInfo approve : approveInfos) {
                    UserInfo approveUser = userService.getById(approve.getApproveUserId());
                    if (approveUser != null) {
                        if (StringUtils.isNotBlank(approveUser.getUcarEmail())) {
                            String ucarEmail = approveUser.getUcarEmail() + "@ucarinc.com";
                            recipient.add(ucarEmail);
                            String luckyEmail = approveUser.getUcarEmail() + "@luckincoffee.com";
                            recipient.add(luckyEmail);
                        }
                    }
                }
            }

            if (applyStatus == SyncApplyStatus.REJECTED || applyStatus == SyncApplyStatus.SUCCEEDED) {
                UserInfo applyUser = userService.getById(applyInfo.getApplyUserId());
                if (applyUser != null) {
                    if (StringUtils.isNotBlank(applyUser.getUcarEmail())) {
                        String ucarEmail = applyUser.getUcarEmail() + "@ucarinc.com";
                        recipient.add(ucarEmail);
                        String luckyEmail = applyUser.getUcarEmail() + "@luckincoffee.com";
                        recipient.add(luckyEmail);
                    }
                }
            }
            mailInfo.setSubject(subject);
            mailInfo.setMailContent(content);
            mailInfo.setRecipient(recipient);
            mailService.sendMail(mailInfo);
        } catch (Exception e) {
            logger.error("Send email failed.", e);
        }
    }

    public static Boolean getMailed(Long applyId, SyncApplyStatus applyStatus) {
        Boolean mailed = applyMailed.getUnchecked(new LoadingKey(applyId, applyStatus));
        return mailed;
    }

    public static Boolean getMailedIfPresent(Long applyId, SyncApplyStatus applyStatus) {
        Boolean present = applyMailed.getIfPresent(new LoadingKey(applyId, applyStatus));
        return present != null;
    }

    private static class LoadingKey{
        private Long applyId;
        private SyncApplyStatus applyStatus;

        public LoadingKey(Long applyId, SyncApplyStatus applyStatus) {
            this.applyId = applyId;
            this.applyStatus = applyStatus;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LoadingKey)) return false;

            LoadingKey that = (LoadingKey) o;

            return applyId.equals(that.applyId) && applyStatus == that.applyStatus;

        }

        @Override
        public int hashCode() {
            int result = applyId.hashCode();
            result = 31 * result + applyStatus.hashCode();
            return result;
        }
    }

}
