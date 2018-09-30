package com.ucar.datalink.manager.core.web.annotation;

import java.lang.annotation.*;

/**
 * Created by sqq on 2018/4/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface AuthIgnore {
}
