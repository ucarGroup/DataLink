package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.menu.MenuInfo;

import java.util.List;

/**
 * Created by sqq on 2017/4/25.
 */
public interface MenuService {

    List<MenuInfo> getList();

    Boolean insert(MenuInfo menuInfo);

    Boolean update(MenuInfo menuInfo);

    Boolean delete(Long id);

    MenuInfo getById(Long id);

    MenuInfo getMenuByUrl(String url);

    List<MenuInfo> getSubMenuList(String parentCode);

    Boolean hasSubLeafMenu(String parentCode);

}
