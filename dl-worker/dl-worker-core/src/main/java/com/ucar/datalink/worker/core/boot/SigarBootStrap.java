package com.ucar.datalink.worker.core.boot;

import com.google.common.io.Resources;
import com.ucar.datalink.common.Constants;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by qianqian.shi on 2018/5/9.
 */
public class SigarBootStrap {

    private static final String WORKER_HOME = System.getProperty(com.ucar.datalink.worker.api.util.Constants.WORKER_HOME);

    public static void boot() throws Exception {
        addSigarPath();
    }

    public static void addSigarPath() throws IOException {
        String sigarPath;
        if (StringUtils.isNotBlank(WORKER_HOME)) {
            sigarPath = WORKER_HOME + "/lib/sigar";
        } else {
            String file = Resources.getResource("sigar/.sigar_shellrc").getFile();
            File classPath = new File(file).getParentFile();
            sigarPath = classPath.getCanonicalPath();
        }

        System.setProperty(Constants.SIGAR_PATH, sigarPath);
    }

}
