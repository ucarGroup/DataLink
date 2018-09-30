package com.ucar.datalink.worker.api.util.compile;

import javax.tools.JavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lubiao on 2017/6/8.
 */
public final class JdkCompileClassLoader extends ClassLoader {

    private final Map<String, JavaFileObject> classes = new HashMap<>();

    public JdkCompileClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Collection<JavaFileObject> files() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public void clearCache() {
        this.classes.clear();
    }

    public JavaFileObject getJavaFileObject(String qualifiedClassName) {
        return classes.get(qualifiedClassName);
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        try {
            c = findClassInternal(name);
        } catch (ClassNotFoundException e) {
            // ClassNotFoundException thrown if class not found
        }

        if (c == null) {
            c = getParent().loadClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        String qualifiedClassName = name.substring(0, name.length() - ".class".length()).replace('/', '.');
        JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
        return new ByteArrayInputStream(file.getByteCode());
    }

    private synchronized Class<?> findClassInternal(String qualifiedClassName) throws ClassNotFoundException {
        JavaFileObject file = classes.get(qualifiedClassName);
        if (file != null) {
            byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
            return defineClass(qualifiedClassName, bytes, 0, bytes.length);
        }
        throw new ClassNotFoundException();
    }

    public void add(String qualifiedClassName, final JavaFileObject javaFile) {
        classes.put(qualifiedClassName, javaFile);
    }
}
