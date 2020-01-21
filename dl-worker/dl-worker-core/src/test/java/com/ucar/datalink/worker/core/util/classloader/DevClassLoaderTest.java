package com.ucar.datalink.worker.core.util.classloader;

import java.net.URL;

/**
 *
 * <p>
 * Created by lubiao on 2017/2/20.
 */
public class DevClassLoaderTest {

    public static void main(String args[]) {
        URL[] urls = DevPluginClassLoader.getPluginClassPath(PluginType.Reader, "dummyreader");
        for (URL url : urls) {
            System.out.println(url.toString());
        }
    }
}
