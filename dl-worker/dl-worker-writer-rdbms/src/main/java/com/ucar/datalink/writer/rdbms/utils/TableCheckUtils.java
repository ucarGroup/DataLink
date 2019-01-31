package com.ucar.datalink.writer.rdbms.utils;

import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;
import com.ucar.datalink.worker.api.util.dialect.DbDialectFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/9/13.
 */
public class TableCheckUtils {

    private static final Logger logger = LoggerFactory.getLogger(TableCheckUtils.class);

    public static Boolean isRowNumLimit(RdbEventRecord record, String columnNameInfo) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        MediaSourceInfo targetMediaSource = mappingInfo.getTargetMediaSource();
        DbDialect dbDialect = DbDialectFactory.getDbDialect(targetMediaSource);
        String targetTableName = StringUtils.isEmpty(mappingInfo.getTargetMediaName()) ? record.getTableName() : mappingInfo.getTargetMediaName();

        String sql = String.format("SHOW TABLE STATUS WHERE NAME LIKE '%s'", targetTableName);
        logger.info("show table status sql is :" + sql);
        Map<String, Object> tableResult = dbDialect.getJdbcTemplate().queryForMap(sql);
        Long rowNum = Long.valueOf(tableResult.get("Rows").toString());
        if (rowNum > 5000000L) {
            logger.info("the row count of table {} is {}, please handle the ddl sql manually.", targetTableName, rowNum);
//            sendMail(record, columnNameInfo, rowNum);
            return true;
        }
        return false;
    }

    private static void sendMail(RdbEventRecord record, String columnNameInfo, Long rowNum) {
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(record);
        MediaInfo srcMedia = mappingInfo.getSourceMedia();
        MediaSourceInfo srcMediaSource = srcMedia.getMediaSource();

        MediaSrcParameter targetMediaSrcPara = mappingInfo.getTargetMediaSource().getParameterObj();
        String targetSchemaName = StringUtils.isEmpty(mappingInfo.getTargetMediaNamespace()) ? targetMediaSrcPara.getNamespace() : mappingInfo.getTargetMediaNamespace();
        String targetTableName = StringUtils.isEmpty(mappingInfo.getTargetMediaName()) ? record.getTableName() : mappingInfo.getTargetMediaName();

        String subject = "Row number in table [" + targetTableName + "] is more than 5000000";
        String content = "hi:" + "<br>" + "<br>" + "&nbsp &nbsp &nbsp &nbsp" + "数据同步的目标表行数为：" + rowNum + "，超过500万，请手动加字段：<br>" + "<br>"
                + "从源库" + srcMediaSource.getName() + "中的表" + srcMedia.getName() + "同步到目标库" + targetSchemaName + "中的表" + targetTableName + "<br>" + "<br>"
                + "目标表缺少字段：" + columnNameInfo + "<br>" + "<br>"
                + "失败的sql语句为：" + record.getSql() + "<br>";

        List<String> recipient = new ArrayList<>();
        List<UserInfo> users = DataLinkFactory.getObject(UserService.class).getUserInfoByRoleType(RoleType.SUPER);
        if (users != null && users.size() > 0) {
            for (UserInfo user : users) {
                if (StringUtils.isNotBlank(user.getUcarEmail())) {
                    String ucarEmail = user.getUcarEmail() + "@ucarinc.com";
                    recipient.add(ucarEmail);
                }
            }
        }
        MailInfo mailInfo = new MailInfo();
        mailInfo.setSubject(subject);
        mailInfo.setMailContent(content);
        mailInfo.setRecipient(recipient);
        DataLinkFactory.getObject(MailService.class).sendMail(mailInfo);
    }
}
