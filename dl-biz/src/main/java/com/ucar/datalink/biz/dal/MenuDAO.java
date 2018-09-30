package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.menu.MenuInfo;

import java.util.List;

/**
 * Created by sqq on 2017/4/25.
 */
public interface MenuDAO {

    List<MenuInfo> getList();

    Integer insert(MenuInfo menuInfo);

    Integer update(MenuInfo menuInfo);

    Integer delete(Long id);

    MenuInfo getById(Long id);

    MenuInfo getMenuByUrl(String url);

    List<MenuInfo> getSubMenuList(String parentCode);
}
