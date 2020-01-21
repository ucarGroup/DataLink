package com.ucar.datalink.manager.core.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Date;


public class DesUtil {

    private final static String DES = "DES";
    private final static String ENCODE = "GBK";
    private final static String defaultKey = "techplat";
    //15分钟
    private final static Long defaultTime = 900000L;


    private static final Logger logger = LoggerFactory.getLogger(TableModeUtil.class);
    /**
     * 使用 默认key 加密
     * @param jo 待加密数据
     * @return
     * @throws Exception
     */
    public static String encrypt(JSONObject jo) throws Exception {
        Date date = new Date();
        jo.put("date",date.getTime());
        byte[] bt = encrypt(jo.toString().getBytes(ENCODE), defaultKey.getBytes(ENCODE));
        String strs = new BASE64Encoder().encode(bt);
        return URLEncoder.encode(strs,"UTF-8");
    }

    /**
     * 使用 默认key 解密
     * @param data 待解密数据
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static JSONObject decrypt(String data){
        if (data == null) {
            return null;
        }
        try {
            Date date = new Date();
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] buf = decoder.decodeBuffer(data);
            byte[] bt = decrypt(buf, defaultKey.getBytes(ENCODE));
            String s = new String(bt, ENCODE);

            JSONObject jo = JSONObject.parseObject(s);
            if(jo.getLong("date")-date.getTime()>defaultTime){
                logger.error("tonken已过期!",new RuntimeException());
                return null;
            }
            if(jo.containsKey("user") && !StringUtils.isEmpty(jo.getString("user"))){
                return jo;
            }
            return null;
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key
     *            加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key 加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }



}