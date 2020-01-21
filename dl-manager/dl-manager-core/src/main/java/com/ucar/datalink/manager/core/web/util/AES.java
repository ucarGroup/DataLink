package com.ucar.datalink.manager.core.web.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.manager.core.web.controller.meta.MetaDataController;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 *
 */
public class AES {
    private static final Logger logger = LoggerFactory.getLogger(AES.class);
    // 加密
    public static String encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            throw new ErrorException(CodeContext.AESKEY_16BITES_ERROR_CODE);
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            throw new ErrorException(CodeContext.AESKEY_16BITES_ERROR_CODE);
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));

        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    public static String decrypt(String sSrc, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                throw new ErrorException(CodeContext.AESKEY_16BITES_ERROR_CODE);
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                throw new ErrorException(CodeContext.AESKEY_16BITES_ERROR_CODE);
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = new Base64().decode(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                logger.info(e.toString());
                return null;
            }
        } catch (Exception ex) {
            logger.info(ex.toString());
            return null;
        }
    }

}