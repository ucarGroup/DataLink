package com.ucar.datalink.worker.api.util.compile;

import com.ucar.datalink.worker.api.util.compile.errors.CompileExprException;
import com.ucar.datalink.worker.api.util.compile.errors.JdkCompileException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Created by lubiao on 2017/6/8.
 */
public class JdkCompiler {

    public Class compile(String sourceString) {
        JavaSource source = new JavaSource(sourceString);
        return compile(source);
    }

    public Class compile(JavaSource javaSource) {
        try {
            final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<>();
            JdkCompileTask compileTask = new JdkCompileTask(new JdkCompileClassLoader(Thread.currentThread().getContextClassLoader()), null);
            String fullName = javaSource.getPackageName() + "." + javaSource.getClassName();
            Class newClass = compileTask.compile(fullName, javaSource.getSource(), errs);
            return newClass;
        } catch (JdkCompileException ex) {
            DiagnosticCollector<JavaFileObject> diagnostics = ex.getDiagnostics();
            throw new CompileExprException("compile error, source : \n" + javaSource + ", "
                    + diagnostics.getDiagnostics(), ex);
        } catch (Exception ex) {
            throw new CompileExprException("compile error, source : \n" + javaSource, ex);
        }

    }
}
