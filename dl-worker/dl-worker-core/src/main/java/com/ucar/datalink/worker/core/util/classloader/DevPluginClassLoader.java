package com.ucar.datalink.worker.core.util.classloader;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 开发版的Plugin-Classloader,支持开发环境不同插件使用不同classloader,方便开发测试，提升效率
 * <p>
 * <p>
 * Important：
 * 1、Reader或Writer下的代码出现变更时，无需进行package操作便可以使用最新的修改，因为这些代码对应的class文件的classpath在每个插件项目的target目录下
 * 2、当Reader或Writer下的pom文件中增加新的jar依赖时，需要执行package操作覆盖根目录下的target，因为插件依赖的jar包的classpath使用的是打包后的路径
 * <p>
 * Created by lubiao on 2017/2/10.
 */
public class DevPluginClassLoader extends AbstractPluginClassLoader {
    private static final String WORKSPACE_PATH_PATTERN = "/dl-worker/dl-worker-core/target/classes/";
    private static final String PLUGIN_LIB_PATH = "/target/dl-worker/plugin/{0}/{1}/lib";
    private static final String PLUGIN_TARGET_PATH = "/dl-worker/dl-worker-{0}/target/classes/";
    private static final String PLUGIN_JAR_NAME_PATTERN = "dl-worker-{0}-";

    public DevPluginClassLoader(URL[] paths) {
        super(paths, DevPluginClassLoader.class.getClassLoader());
    }

    public static URL[] getPluginClassPath(PluginType pluginType, String pluginName) {
        URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        URL[] urls = cl.getURLs();
        for (URL u : urls) {
            String path = u.toString();
            if (path.contains(WORKSPACE_PATH_PATTERN)) {
                return getJarURLs(path, pluginType, pluginName);
            }
        }
        throw new DatalinkException("plugin class path can not found.");
    }

    private static URL[] getJarURLs(String path, PluginType pluginType, String pluginName) {
        String basePath = StringUtils.substringBefore(path, WORKSPACE_PATH_PATTERN);
        String pluginLibPath = basePath + MessageFormat.format(PLUGIN_LIB_PATH, pluginType.toString().toLowerCase(), pluginName);
        String pluginClassesPath = basePath + MessageFormat.format(PLUGIN_TARGET_PATH, pluginName);

        /* set filter */
        FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");

		/* iterate all jar */
        File[] allJars;
        try {
            allJars = new File(new URI(pluginLibPath)).listFiles(jarFilter);
        } catch (URISyntaxException e) {
            throw new DatalinkException("invalid plugin url.", e);
        }

        //为了兼容没有lib包的情况
        if(allJars == null){
            URL url;
            try {
                url = new URL(pluginClassesPath);
            } catch (MalformedURLException e) {
                throw new DatalinkException("invalid url.", e);
            }
            return new URL[]{url};
        }

        List<URL> jarURLs = new ArrayList<>(allJars.length);
        for (File file : allJars) {
            try {
                if (file.getName().contains(MessageFormat.format(PLUGIN_JAR_NAME_PATTERN, pluginName))) {
                    continue;//过滤掉plugin对应的jar包，应该用项目中target目录下的class文件
                }

                jarURLs.add(file.toURI().toURL());
            } catch (Exception e) {
                throw new DatalinkException("something goes wrong when parse urls.", e);
            }
        }

        //added local classes
        try {
            jarURLs.add(new URL(pluginClassesPath));
        } catch (MalformedURLException e) {
            throw new DatalinkException("invalid url.", e);
        }

        URL[] result = new URL[jarURLs.size()];
        jarURLs.toArray(result);
        return result;
    }
}
