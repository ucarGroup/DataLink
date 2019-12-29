package com.ucar.datalink.common.event;

import java.lang.annotation.*;

/**
 * Created by sqq on 2018/8/22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface EventHandler {
}
