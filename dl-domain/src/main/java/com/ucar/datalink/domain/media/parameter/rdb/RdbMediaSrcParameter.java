package com.ucar.datalink.domain.media.parameter.rdb;

import com.alibaba.fastjson.annotation.JSONField;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 关系型数据库参数类
 * <p>
 * Created by user on 2017/3/3.
 */
public class RdbMediaSrcParameter extends MediaSrcParameter {
    private int port;
    private String desc;//描述
    private String name;//数据源名称
    private String encoding = "UTF-8";//编码
    private String driver;//驱动类名
    private WriteConfig writeConfig;
    private ReadConfig readConfig;
    private Object dataSourceConfig;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public WriteConfig getWriteConfig() {
        return writeConfig;
    }

    public void setWriteConfig(WriteConfig writeConfig) {
        this.writeConfig = writeConfig;
    }

    public ReadConfig getReadConfig() {
        return readConfig;
    }

    public void setReadConfig(ReadConfig readConfig) {
        this.readConfig = readConfig;
    }

    public Object getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(Object dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public static class WriteConfig {
        private String writeHost;
        private String username;
        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getWriteHost() {
            return writeHost;
        }

        public void setWriteHost(String writeHost) {
            this.writeHost = writeHost;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @JSONField(serialize = false)
        public String getDecryptPassword() {
            String decryptPassword = "";
            if(StringUtils.isNotBlank(password)){
                decryptPassword  = DbConfigEncryption.decrypt(password);
            }
            return decryptPassword;
        }

        @JSONField(serialize = false)
        public void setEncryptPassword(String password) {
            if(StringUtils.isNotBlank(password)){
                this.password = DbConfigEncryption.encrypt(password);
            }else{
                this.password = "";
            }
        }
    }

    public static class ReadConfig {
        private List<String> hosts;
        private String username;//用户名
        private String password;//密码
        private String etlHost;

        public List<String> getHosts() {
            return hosts;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEtlHost() {
            return etlHost;
        }

        public void setEtlHost(String etlHost) {
            this.etlHost = etlHost;
        }

        @JSONField(serialize = false)
        public String getDecryptPassword() {
            String decryptPassword = "";
            if(StringUtils.isNotBlank(password)){
                decryptPassword  = DbConfigEncryption.decrypt(password);
            }
            return decryptPassword;
        }

        @JSONField(serialize = false)
        public void setEncryptPassword(String password) {
            if(StringUtils.isNotBlank(password)){
                this.password = DbConfigEncryption.encrypt(password);
            }else{
                this.password = "";
            }
        }
    }
}
