package com.ucar.datalink.worker.api.intercept;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.InterceptorService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.interceptor.InterceptorInfo;
import com.ucar.datalink.domain.interceptor.InterceptorType;
import com.ucar.datalink.worker.api.util.Constants;
import com.ucar.datalink.worker.api.util.scan.ClassScanner;
import com.ucar.datalink.worker.api.util.scan.ClasspathClassScanner;
import com.ucar.datalink.worker.api.util.scan.FileSystemClassScanner;
import com.ucar.datalink.worker.api.util.compile.JdkCompiler;
import com.ucar.datalink.worker.api.util.compile.JavaSource;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Created by lubiao on 2017/3/22.
 * 拦截器工厂类，根据给定的interceptorId，实例化对应的Interceptor实例.
 */
public class InterceptorFactory {

    private final static JdkCompiler jdkCompiler;
    private final static LoadingCache<Long, Interceptor> interceptors;
    private final static ClassScanner classPathScanner;
    private final static ClassScanner fileSystemScanner;

    static {
        jdkCompiler = new JdkCompiler();
        interceptors = CacheBuilder
                .newBuilder()
                .build(new CacheLoader<Long, Interceptor>() {

                    @Override
                    public Interceptor load(Long key) throws Exception {
                        return (Interceptor) getInterceptorInternal(key);
                    }
                });
        classPathScanner = new ClasspathClassScanner();
        fileSystemScanner = new FileSystemClassScanner(System.getProperty(Constants.WORKER_HOME) + File.separator + "extend");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> Interceptor<T> getInterceptor(Long interceptorId) {
        return interceptors.getUnchecked(interceptorId);
    }

    private static Object getInterceptorInternal(Long key) {
        InterceptorInfo info = DataLinkFactory.getObject(InterceptorService.class).getInterceptorById(key);
        Class<?> clazz = null;
        String fullname = StringUtils.EMPTY;

        if (info.getType() == InterceptorType.Class && StringUtils.isNotBlank(info.getContent())) {
            clazz = scan(info.getContent());
            fullname = "[" + info.getContent() + "]ClassPath";
        } else if (info.getType() == InterceptorType.Script && StringUtils.isNotBlank(info.getContent())) {
            JavaSource javaSource = new JavaSource(info.getContent());
            clazz = jdkCompiler.compile(javaSource);
            fullname = "[" + javaSource.toString() + "]SourceText";
        }

        if (clazz == null) {
            throw new InterceptorLoadException("ERROR ## classload this interceptor=" + fullname + " has an error");
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new InterceptorLoadException("ERROR ## classload this interceptor=" + fullname + " has an error", e);
        }
    }

    private static Class scan(String fileResolverClassname) {
        Class<?> clazz = classPathScanner.scan(fileResolverClassname);
        if (clazz == null) {
            clazz = fileSystemScanner.scan(fileResolverClassname);
        }

        return clazz;
    }

    public static void invalidateAll() {
        interceptors.invalidateAll();
    }

    public static void invalidateOne(Long interceptorId) {
        interceptors.invalidate(interceptorId);
    }
}
