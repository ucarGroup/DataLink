package com.ucar.datalink.biz.utils;

import com.ucar.datalink.common.utils.DbConfigEncryption;
import org.springframework.util.DefaultPropertiesPersister;

import java.io.*;
import java.util.Properties;

/**
 * Created by lubiao on 2017/4/19.
 */
public class DLPropertiesPersister extends DefaultPropertiesPersister {
    private static final String PASSWORD_KEY = "datasource.password";

    @Override
    public void load(Properties props, InputStream is) throws IOException {

        Properties properties = new Properties();
        properties.load(is);

        if (properties.get(PASSWORD_KEY) != null) {
            String password;
            try {
                password = DbConfigEncryption.decrypt(properties.getProperty(PASSWORD_KEY));
            } catch (Exception e) {
                password = properties.getProperty(PASSWORD_KEY);
            }
            properties.setProperty(PASSWORD_KEY, password);
        }
        OutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            properties.store(outputStream, "");
            is = outStream2InputStream(outputStream);
            super.load(props, is);
        } catch (IOException e) {
            throw e;
        } finally {
            outputStream.close();
        }
    }


    private InputStream outStream2InputStream(OutputStream out) {
        ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
