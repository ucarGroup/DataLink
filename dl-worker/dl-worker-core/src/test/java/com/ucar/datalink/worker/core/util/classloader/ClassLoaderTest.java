package com.ucar.datalink.worker.core.util.classloader;

import sun.misc.Launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * Somthing basic test for classloader-knowledge.
 *
 * Created by lubiao on 2017/2/10.
 */
public class ClassLoaderTest {
    public static void main(String args[]) {
        HashMap<Long,String> map =new HashMap<>();
        map.put(new Long("1"),"1");
        map.put(1L,"2");
        System.out.println(map.size());
    }

    private static void testClasspathProperty() {
        System.out.println(System.getProperty("java.class.path"));
    }

    private static void testUrlClassLoader() {
        URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] urls = cl.getURLs();
        for (URL u : urls) {
            System.out.println(u.toString());
        }
    }

    private static void testBootstrapClassLoader() {
        URL[] urls = Launcher.getBootstrapClassPath().getURLs();
        for (URL u : urls) {
            System.out.println(u.toString());
        }
    }
}
