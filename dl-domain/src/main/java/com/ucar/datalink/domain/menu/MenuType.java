package com.ucar.datalink.domain.menu;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by sqq on 2017/4/25.
 */
public enum MenuType {
    NODE, LEAF, ACTION;

    public static List<MenuType> getAllMenuTypes() {
        return Lists.newArrayList(NODE, LEAF, ACTION);
    }
}
