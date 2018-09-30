package com.ucar.datalink.worker.core.util.classloader;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.worker.api.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassLoader工厂类.
 * 开发环境使用DevClassLoader，正式环境使用PluginClassLoader.
 * <p>
 * Created by lubiao on 2017/2/15.
 */
public class ClassLoaderFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderFactory.class);

    private static final String WORKER_HOME = System.getProperty(Constants.WORKER_HOME);

    private static final String PLUGIN_TYPE_NAME_FORMAT = "plugin.%s.%s";

    private static final String PLUGIN_READER_HOME = StringUtils.join(new String[]{WORKER_HOME, "plugin", "reader"}, File.separator);

    private static final String PLUGIN_WRITER_HOME = StringUtils.join(new String[]{WORKER_HOME, "plugin", "writer"}, File.separator);

    private static final Map<String, RelPluginClassLoader> relClassLoaderCache = new ConcurrentHashMap<>();

    private static final Map<String, DevPluginClassLoader> devClassLoaderCache = new ConcurrentHashMap<>();

    public static ClassLoader getClassLoader(PluginType pluginType, String pluginName, String classLoaderType) {
        if ("Dev".equals(classLoaderType)) {
            return getDevPluginClassloader(pluginType, pluginName);
        } else if ("Rel".equals(classLoaderType)) {
            return getRelPluginClassloader(pluginType, pluginName);
        } else if ("Inherit".equals(classLoaderType)) {
            return Thread.currentThread().getContextClassLoader();//主要调试程序用
        } else {
            throw new DatalinkException("invalid classloader type.");
        }
    }

    private static synchronized ClassLoader getRelPluginClassloader(PluginType pluginType, String pluginName) {
        String pluginKey = generatePluginKey(pluginType, pluginName);
        RelPluginClassLoader cl = relClassLoaderCache.get(pluginKey);
        if (cl == null) {
            if (pluginType == PluginType.Reader) {
                cl = new RelPluginClassLoader(new String[]{PLUGIN_READER_HOME + File.separator + pluginName});
            } else {
                cl = new RelPluginClassLoader(new String[]{PLUGIN_WRITER_HOME + File.separator + pluginName});
            }

            logger.info(String.format("Classloader for [%s] is instanced.", pluginKey));
            relClassLoaderCache.put(pluginKey, cl);
        }
        return cl;
    }

    private static synchronized ClassLoader getDevPluginClassloader(PluginType pluginType, String pluginName) {
        String pluginKey = generatePluginKey(pluginType, pluginName);
        DevPluginClassLoader cl = devClassLoaderCache.get(pluginKey);
        if (cl == null) {
            cl = new DevPluginClassLoader(DevPluginClassLoader.getPluginClassPath(pluginType, pluginName));
            devClassLoaderCache.put(pluginKey, cl);
        }
        return cl;
    }

    private static String generatePluginKey(PluginType pluginType,
                                            String pluginName) {
        return String.format(PLUGIN_TYPE_NAME_FORMAT, pluginType.toString(), pluginName);
    }
}
