package com.ucar.datalink.writer.kudu.util;

import org.apache.kudu.Type;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KuduUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(KuduUtils.class);

    public static KuduClient createClient(List<String> masterAddresses) {
        try {
            KuduClient build = new KuduClient.KuduClientBuilder(masterAddresses).build();
            return build;
        } catch (Exception e) {
            throw new RuntimeException("链接异常!");
        }
    }

    public static void closeClient(KuduClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (KuduException e) {
            }
        }
    }

    public static Type getKuduType(String typeName) {
        if (typeName == null || typeName.trim() == "") {
            return null;
        }

        String lowercaseTypeNmae = typeName.toLowerCase();

        if (lowercaseTypeNmae.startsWith("char") || lowercaseTypeNmae.startsWith("varchar") || lowercaseTypeNmae.contains("text") || lowercaseTypeNmae.contains("blob")) {
            return Type.STRING;
        } else if (lowercaseTypeNmae.equals("tinyint")) {
            return Type.INT8;
        } else if (lowercaseTypeNmae.equals("smallint")) {
            return Type.INT16;
        } else if (lowercaseTypeNmae.equals("mediumint") || lowercaseTypeNmae.equals("int")) {
            return Type.INT32;
        } else if (lowercaseTypeNmae.equals("bigint")) {
            return Type.INT64;
        } else if (lowercaseTypeNmae.equals("bigint")) {
            return Type.INT64;
        } else if (lowercaseTypeNmae.contains("float")) {
            return Type.FLOAT;
        } else if (lowercaseTypeNmae.contains("double")) {
            return Type.DOUBLE;
        } else if (lowercaseTypeNmae.contains("double")) {
            return Type.DOUBLE;
        } else if (lowercaseTypeNmae.contains("date") || lowercaseTypeNmae.contains("time") || lowercaseTypeNmae.contains("datetime") || lowercaseTypeNmae.contains("timestamp")) {
            return Type.STRING;
        } else {
            return null;
        }
    }


}
