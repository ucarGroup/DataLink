package com.ucar.datalink.manager.core.web.util;

import com.ucar.datalink.domain.user.UserInfo;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
public class UserUtil {
    public static Long getUserIdFromRequest(){
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        UserInfo user = (UserInfo)request.getSession().getAttribute("user");
        return user.getId();
    }
}
