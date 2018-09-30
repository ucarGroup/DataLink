package com.ucar.datalink.worker.core.util.classloader;

import com.ucar.datalink.common.errors.DatalinkException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 正式版的Plugin-Classloader,负责从各个插件目录下的jar包中加载类文件.
 * <p>
 * Created by lubiao on 2017/2/24.
 */
public class RelPluginClassLoader extends AbstractPluginClassLoader {

    public RelPluginClassLoader(String[] paths) {
        this(paths, RelPluginClassLoader.class.getClassLoader());
    }

    public RelPluginClassLoader(String[] paths, ClassLoader parent) {
        super(buildURLs(paths), parent);
    }

    private static URL[] buildURLs(String[] paths) {
        Validate.isTrue(null != paths && 0 != paths.length, "Paths can not be null.");

        List<String> dirs = new ArrayList<>();
        for (String path : paths) {
            dirs.add(path);
            RelPluginClassLoader.collectDirs(path, dirs);
        }

        List<URL> urls = new ArrayList<>();
        for (String path : dirs) {
            urls.addAll(doGetURLs(path));
        }

        return urls.toArray(new URL[0]);
    }

    private static void collectDirs(String path, List<String> collector) {
        if (null == path || StringUtils.isBlank(path)) {
            return;
        }

        File current = new File(path);
        if (!current.exists() || !current.isDirectory()) {
            return;
        }

        for (File child : current.listFiles()) {
            if (!child.isDirectory()) {
                continue;
            }

            collector.add(child.getAbsolutePath());
            collectDirs(child.getAbsolutePath(), collector);
        }
    }

    private static List<URL> doGetURLs(final String path) {
        Validate.isTrue(!StringUtils.isBlank(path), "Paths can not be null.");

        File jarPath = new File(path);

        Validate.isTrue(jarPath.exists() && jarPath.isDirectory(), "Path must exist and shoud be a directory.{" + path + "}");

		/* set filter */
        FileFilter jarFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        };

		/* iterate all jar */
        File[] allJars = new File(path).listFiles(jarFilter);
        List<URL> jarURLs = new ArrayList<URL>(allJars.length);

        for (int i = 0; i < allJars.length; i++) {
            try {
                jarURLs.add(allJars[i].toURI().toURL());
            } catch (Exception e) {
                throw new DatalinkException("something goes wrong when load jars.");
            }
        }

        return jarURLs;
    }
}
