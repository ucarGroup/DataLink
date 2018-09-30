package com.ucar.datalink.reader.hbase.replicate;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Strings;
import org.apache.hadoop.net.DNS;

import java.net.UnknownHostException;
import java.util.function.Supplier;

/**
 * Created by lubiao on 2017/11/16.
 */
public class HostnameSupplier implements Supplier<String> {

    private static final String DEFAULT_LOCALHOST = "localhost";
    private static final String DEFAULT_DNS_INTERFACE = "default";

    private Configuration hbaseConfig;

    public HostnameSupplier(final Configuration hbaseConfig) {
        this.hbaseConfig = hbaseConfig;
    }

    @Override
    public String get() {
        try {
            final String dnsInterface = hbaseConfig.get("hbase.regionserver.dns.interface", DEFAULT_DNS_INTERFACE);
            String hostname = Strings.domainNamePointerToHostName(DNS.getDefaultHost(dnsInterface));
            return hostname;
        } catch( UnknownHostException ukhe) {
            throw new RuntimeException(ukhe);
        }
    }
}
