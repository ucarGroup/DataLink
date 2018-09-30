package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.MenuDAO;
import com.ucar.datalink.biz.service.MenuService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.domain.menu.MenuInfo;
import com.ucar.datalink.domain.menu.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sqq on 2017/4/25.
 */
@Service
public class MenuServiceImpl implements MenuService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MenuServiceImpl.class);

    @Autowired
    MenuDAO menuDAO;

    @Autowired
    RoleService roleService;

    @Override
    public List<MenuInfo> getList() {
        return menuDAO.getList();
    }

    @Override
    public Boolean insert(MenuInfo menuInfo) {
        Integer num = menuDAO.insert(menuInfo);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public Boolean update(MenuInfo menuInfo) {
        Integer num = menuDAO.update(menuInfo);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = menuDAO.delete(id);
        if (num > 0) {
            return true;
        }

        return false;
    }

    @Override
    public MenuInfo getById(Long id) {
        return menuDAO.getById(id);
    }

    @Override
    public MenuInfo getMenuByUrl(String url) {
        return menuDAO.getMenuByUrl(url);
    }

    @Override
    public List<MenuInfo> getSubMenuList(String parentCode) {
        List<MenuInfo> list = menuDAO.getSubMenuList(parentCode);
        return list;
    }

    @Override
    public Boolean hasSubLeafMenu(String parentCode) {
        List<MenuInfo> subList = getSubMenuList(parentCode);
        if (subList != null) {
            for (MenuInfo menuInfo : subList) {
                if (menuInfo.getType() == MenuType.LEAF) {
                    return true;
                }
            }
        }
        return false;
    }

}
