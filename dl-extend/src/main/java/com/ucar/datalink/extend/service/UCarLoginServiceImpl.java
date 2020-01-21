package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.LoginService;
import com.ucar.datalink.domain.user.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Created by lubiao on 2018/3/1.
 */
@Service
public class UCarLoginServiceImpl implements LoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UCarLoginServiceImpl.class);

    @Override
    public boolean checkUcarPassWord(UserInfo userInfo, String password) {
        String loginEmail = userInfo.getUcarEmail();

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://10.2.208.11");//10.100.20.11
        env.put(Context.SECURITY_PRINCIPAL, "ou=总部集团,ou=神州优车集团,dc=ucarinc,dc=com");//cn=vpn,ou=Groups,dc=zuche,dc=intra
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "ucarinc\\" + loginEmail);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            return true;
        } catch (Exception e) {
            LOGGER.error("登录认证失败", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    LOGGER.error("登录认证失败", e);
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkLuckyPassWord(UserInfo userInfo, String password) {
        String loginEmail = userInfo.getUcarEmail();

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://10.2.108.21");//10.100.20.11
        env.put(Context.SECURITY_PRINCIPAL, "dc=luckin,dc=com");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "luckincoffee\\"+loginEmail);
        env.put(Context.SECURITY_CREDENTIALS, password);

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            if (ctx != null) {
                return true;
            }
        } catch (Exception e) {
            //LOGGER.error("登录认证失败", e);
            LOGGER.error("登录认证失败 loginEmail="+loginEmail, e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    LOGGER.error("登录认证失败", e);
                }
            }
        }
        return false;
    }
}
