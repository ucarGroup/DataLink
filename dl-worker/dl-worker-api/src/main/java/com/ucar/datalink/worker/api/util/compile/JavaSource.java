package com.ucar.datalink.worker.api.util.compile;

import org.apache.commons.lang.StringUtils;

/**
 * Java源文件模型类.
 * 
 * Created by lubiao on 2017/6/8.
 */
public class JavaSource {

    private String packageName;
    private String className;
    private String source;

    public JavaSource(String sourceString) {
        String className = RegexUtils
                .findFirst(sourceString, "public class (?s).*?{")
                .split("extends")[0]
                .split("implements")[0]
                .replaceAll("public class ", StringUtils.EMPTY)
                .replace("{", StringUtils.EMPTY)
                .trim();
        String packageName = RegexUtils.
                findFirst(sourceString, "package (?s).*?;")
                .replaceAll("package ", StringUtils.EMPTY)
                .replaceAll(";", StringUtils.EMPTY)
                .trim();
        this.packageName = packageName;
        this.className = className;
        this.source = sourceString;
    }

    public JavaSource(String packageName, String className, String source) {
        this.packageName = packageName;
        this.className = className;
        this.source = source;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        return packageName + "." + className;
    }
}
