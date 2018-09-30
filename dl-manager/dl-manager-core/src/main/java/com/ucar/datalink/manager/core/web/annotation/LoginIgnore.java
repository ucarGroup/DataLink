package com.ucar.datalink.manager.core.web.annotation;

import java.lang.annotation.*;

/**
 * Created by sqq on 2018/4/10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface LoginIgnore {
}
