package com.ucar.datalink.worker.core.util.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Class Loader for TaskReaders & TaskWriteres.
 * 破坏了双亲委派模型，优先从自己的插件目录中加载class,查找不到时再从父加载器加载.
 * <p>
 * Created by lubiao on 2017/3/3.
 */
public class AbstractPluginClassLoader extends URLClassLoader {
    public AbstractPluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public AbstractPluginClassLoader(URL[] urls) {
        super(urls);
    }

    public AbstractPluginClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            long t0 = System.nanoTime();
            if (!isForceParent(name)) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the plugin lib path.
                }
            }

            if (c == null) {
                // If still not found, then invoke loadClass method of parent in order
                // to find the class.
                long t1 = System.nanoTime();

                c = getParent().loadClass(name);

                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);

        // if local search failed, delegate to parent
        if (url == null) {
            url = getParent().getResource(name);
        }
        return url;
    }

    //定义白名单，名单内的强制走父类加载器
    //解决由于插件和框架都引用了相同jar包导致的 "java.lang.LinkageError" 错误
    //java.lang.LinkageError原理介绍：https://stackoverflow.com/questions/18127431/spring-java-lang-linkageerror-loader-constraint-violation-loader-previously-in

    private static final List<String> packageWhiteList = new ArrayList<>();

    static {
        packageWhiteList.add("com.google.common.eventbus.");
    }

    private boolean isForceParent(String name) {
        return packageWhiteList.stream().filter(i -> name.startsWith(i)).findFirst().isPresent();
    }
}
