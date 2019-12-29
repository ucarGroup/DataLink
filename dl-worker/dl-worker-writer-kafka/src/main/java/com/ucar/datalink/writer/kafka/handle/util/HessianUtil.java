package com.ucar.datalink.writer.kafka.handle.util;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.ucar.datalink.common.errors.DatalinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Auther: zhaoxinguo
 * @Date: 2018/8/23 10:48
 * @Description: Hessian实现序列化、反序列化
 */
public class HessianUtil {
    private static final Logger logger = LoggerFactory.getLogger(HessianUtil.class);

    /**
     * Hessian实现序列化
     */
    public static byte[] serialize(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        HessianOutput hessianOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            // Hessian的序列化输出
            hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new DatalinkException("Hessian Serialize failed.");
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.error("close failed.", e);
                }
            }
            if (hessianOutput != null) {
                try {
                    hessianOutput.close();
                } catch (IOException e) {
                    logger.error("close failed.", e);
                }
            }
        }
    }

    /**
     * Hessian实现反序列化
     */
    public static <T> T deserialize(byte[] employeeArray) {
        ByteArrayInputStream byteArrayInputStream = null;
        HessianInput hessianInput = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(employeeArray);
            // Hessian的反序列化读取对象
            hessianInput = new HessianInput(byteArrayInputStream);
            return (T) hessianInput.readObject();
        } catch (IOException e) {
            throw new DatalinkException("Hessian Deserialize failed.");
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    logger.error("close failed.", e);
                }
            }
            if (hessianInput != null) {
                try {
                    hessianInput.close();
                } catch (Exception e) {
                    logger.error("close failed.", e);
                }
            }
        }
    }
}