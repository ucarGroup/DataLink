package com.ucar.datalink.domain.media.parameter.kudu;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

import java.util.ArrayList;
import java.util.List;

public class KuduMediaSrcParameter extends MediaSrcParameter {

    private String database;
    private int bufferSize;
    private List<KuduMasterConfig> kuduMasterConfigs;
    private List<ImpalaCconfig> impalaCconfigs;


    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public List<KuduMasterConfig> getKuduMasterConfigs() {
        return kuduMasterConfigs;
    }

    public void setKuduMasterConfigs(List<KuduMasterConfig> kuduMasterConfigs) {
        this.kuduMasterConfigs = kuduMasterConfigs;
    }

    public List<ImpalaCconfig> getImpalaCconfigs() {
        return impalaCconfigs;
    }

    public void setImpalaCconfigs(List<ImpalaCconfig> impalaCconfigs) {
        this.impalaCconfigs = impalaCconfigs;
    }

    public List<String> getHost2Ports(){
        ArrayList<String> host2Ports = new ArrayList<>();
        if(kuduMasterConfigs != null ){
            for(KuduMasterConfig k :kuduMasterConfigs){
                host2Ports.add(String.format("%s:%d",k.getHost(),k.getPort()));
            }
        }

        return host2Ports;
    }


    public static class KuduMasterConfig {
        private String host;
        private int port;

        public KuduMasterConfig(){}

        public KuduMasterConfig(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public KuduMasterConfig setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public KuduMasterConfig setPort(int port) {
            this.port = port;
            return this;
        }
    }



    public static class ImpalaCconfig {
        private String host;
        private String port;

        public ImpalaCconfig(){}

        public ImpalaCconfig(String host, String port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        @Override
        public String toString() {
            return "ImpalaCconfig{" +
                    "host='" + host + '\'' +
                    ", port='" + port + '\'' +
                    '}';
        }
    }



}
