package com.ucar.datalink.manager.core.web.authority;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yw.zhang02 on 2016/9/16.
 */
public class MenuManager {

    private static Map<String, Menu> urlMenuMap = new HashMap<String, Menu>();
    private static Map<String, Menu> codeMenuMap = new HashMap<String, Menu>();
    private static Map<String, List<Menu>> subMenuListMap = new HashMap<String, List<Menu>>();

    static {

        for(Object[] objectItems : MenuDatas.menuArr){
            String code = (String)objectItems[0];
            String name = (String)objectItems[1];
            String parentCode = (String)objectItems[2];
            String type = (String)objectItems[3];
            String url = (String)objectItems[4];
            String icon = (String)objectItems[5];
            String roles = (String)objectItems[6];

            Menu menu = new Menu();
            menu.setCode(code);
            menu.setName(name);
            menu.setParentCode(parentCode);
            menu.setUrl(url);
            menu.setType(type);
            menu.setIcon(icon);
            menu.setRoles(Sets.newHashSet(roles.split(",")));
            List<Menu> subMenuList = subMenuListMap.get(parentCode);
            if(subMenuList == null){
                subMenuList = new ArrayList<Menu>();
                subMenuListMap.put(parentCode, subMenuList);
            }
            subMenuList.add(menu);
            codeMenuMap.put(code, menu);
            if(url != null) {
                urlMenuMap.put(url, menu);
            }
        }
    }

    public static Boolean hasSubLeafMenu(String code){
        List<Menu> subList = getSubMenuList(code);
        if(subList != null){
            for(Menu sub : subList){
                if (sub.getType().equals("leaf")){
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Menu> getRootSubMenuList(){
        return getSubMenuList("000000000");
    }

    public static List<Menu> getSubMenuList(String code){
        return subMenuListMap.get(code);
    }

    public static Menu getMenuByUrl(String url){
        return urlMenuMap.get(url);
    }

    public static Menu getMenuByCode(String code){
        return codeMenuMap.get(code);
    }

    public static boolean codeHasPermission(String code, String role){
        Menu menu = codeMenuMap.get(code);
        return hasPermission(menu, role);
    }

    public static boolean urlHasPermission(String url, String role){
        Menu menu = urlMenuMap.get(url);
        return hasPermission(menu, role);
    }

    public static boolean hasPermission(Menu menu, String role){
        if(menu == null){
            return false;
        }
        return menu.getRoles().contains(role);
    }
}
