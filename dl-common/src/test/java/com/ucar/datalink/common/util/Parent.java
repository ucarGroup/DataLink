package com.ucar.datalink.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2018/3/12.
 */
public class Parent {

    int age;//--4
    String name;//--12,""--32
    Children children = new Children();//24+4--28
    List<String> list = new ArrayList<>();//36+4--40,""--72
    Map<Long, String> map = new HashMap<>();//40+4--44
}
