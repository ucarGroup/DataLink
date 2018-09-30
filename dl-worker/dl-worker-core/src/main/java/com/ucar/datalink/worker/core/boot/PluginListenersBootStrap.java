package com.ucar.datalink.worker.core.boot;

import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.domain.plugin.PluginParameter;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.worker.core.runtime.WorkerConfig;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderFactory;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderSwapper;
import com.ucar.datalink.worker.core.util.classloader.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 负责启动每个插件下的PluginListener，PluginListener负责接收Event，完成插件的"增值任务",如：提供元数据
 *
 * Created by user on 2017/6/21.
 */
public class PluginListenersBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(PluginListenersBootStrap.class);

    public static void boot(WorkerConfig workerConfig) throws Exception {
        Class readerClass = PluginReaderParameter.class;
        Class writerClass = PluginWriterParameter.class;

        initSubClasses(readerClass, workerConfig, PluginType.Reader);
        initSubClasses(writerClass, workerConfig, PluginType.Writer);
    }

    public static void initSubClasses(Class<?> cls, WorkerConfig workerConfig, PluginType pluginType) throws Exception {
        ClassLoaderSwapper classLoaderSwapper = ClassLoaderSwapper.newCurrentThreadClassLoaderSwapper();

        List<Class<?>> subClasses = getAllAssignedClass(cls);
        for (Class<?> clazz : subClasses) {
            PluginParameter pp = ((PluginParameter) clazz.newInstance());
            String pluginListenerClassName = pp.getPluginListenerClass();
            ClassLoader classLoader = ClassLoaderFactory.getClassLoader(pluginType, pp.getPluginName(), workerConfig.getString(WorkerConfig.CLASSLOADER_TYPE_CONFIG));
            Class<?> pluginListenerClass = classLoader.loadClass(pluginListenerClassName);
            PluginListener pluginListener = (PluginListener) pluginListenerClass.newInstance();

            try {
                classLoaderSwapper.setCurrentThreadClassLoader(classLoader);
                logger.info(
                        String.format("Prepare to initial plugin listener.PluginType is [%s],PluginName is [%s],ClassLoader is [%s],PluginListener is [%s]",
                                pluginType, pp.getPluginName(), classLoader.getClass().getSimpleName(), pluginListenerClassName));
                pluginListener.init();
                logger.info(String.format("Plugin listener %s is initialized.", pluginListenerClassName));
            } finally {
                classLoaderSwapper.restoreCurrentThreadClassLoader();
            }
        }
    }

    public static List<Class<?>> getAllAssignedClass(Class<?> cls) throws IOException,
            ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> c : getClasses(cls)) {
            if (cls.isAssignableFrom(c) && !cls.equals(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    public static List<Class<?>> getClasses(Class<?> cls) throws IOException,
            ClassNotFoundException {
        String pk = cls.getPackage().getName();
        String path = pk.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        String protocol = url.getProtocol();
        if ("jar".equalsIgnoreCase(protocol)) {
            return getClassesFromJar(url, pk);
        } else {
            return getClassesFromDir(new File(url.getFile()), pk);
        }
    }

    private static List<Class<?>> getClassesFromJar(URL url, String basePack) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        JarURLConnection connection = (JarURLConnection) url.openConnection();
        if (connection != null) {
            JarFile jarFile = connection.getJarFile();
            if (jarFile != null) {
                //得到该jar文件下面的类实体
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry entry = jarEntryEnumeration.nextElement();
                    String jarEntryName = entry.getName();
                    //这里我们需要过滤不是class文件和不在basePack包名下的类
                    if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/", ".").startsWith(basePack)) {
                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                        Class cls = Class.forName(className);
                        classes.add(cls);
                    }
                }
            }
        }

        return classes;
    }

    private static List<Class<?>> getClassesFromDir(File dir, String pk) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!dir.exists()) {
            return classes;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                classes.addAll(getClassesFromDir(f, pk + "." + f.getName()));
            }
            String name = f.getName();
            if (name.endsWith(".class")) {
                classes.add(Class.forName(pk + "." + name.substring(0, name.length() - 6)));
            }
        }
        return classes;
    }

}
