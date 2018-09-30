<%@ page import="java.util.List" %>
<%@ page import="com.ucar.datalink.domain.menu.MenuInfo" %>
<%@ page import="com.ucar.datalink.biz.service.MenuService" %>
<%@ page import="com.ucar.datalink.biz.service.RoleService" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="com.ucar.datalink.domain.menu.MenuType" %>
<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="com.ucar.datalink.domain.user.UserInfo" %>
<%@ page import="com.ucar.datalink.biz.service.UserService" %>
<%@ page contentType="text/html; charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<div class="sidebar                  responsive                    ace-save-state" id="sidebar">
    <script type="text/javascript">
        try {
            ace.settings.loadState('sidebar')
        } catch (e) {
        }
    </script>

    <ul class="nav nav-list">
        <li>
            <a href="${basePath}/" id="aMain">
                <i class="menu-icon fa fa-home"></i>
                <span class="menu-text"> 主页 </span>
            </a>

            <b class="arrow"></b>
        </li>

        <%
            MenuService menuService = WebApplicationContextUtils.getWebApplicationContext(application).getBean(MenuService.class);
            RoleService roleService = WebApplicationContextUtils.getWebApplicationContext(application).getBean(RoleService.class);
            UserService userService = WebApplicationContextUtils.getWebApplicationContext(application).getBean(UserService.class);
            UserInfo userInfo = (UserInfo) session.getAttribute("user");
            //是否超级管理员
            Boolean isSuper = userService.isSuper(userInfo);

            List<MenuInfo> menuList = menuService.getSubMenuList("000000000");
            if (menuList != null) {
                for (MenuInfo menu : menuList) {
                    Boolean hasRole = roleService.hasRole(menu.getId(), userInfo);
                    if (hasRole || isSuper) {
        %>
        <li class="">
            <a href="#" class="dropdown-toggle">
                <i class="menu-icon fa <%=menu.getIcon()%>"></i>
                <span class="menu-text">
								<%=menu.getName()%>
							</span>

                <b class="arrow fa fa-angle-down"></b>
            </a>

            <b class="arrow"></b>
            <%
                List<MenuInfo> subList = menuService.getSubMenuList(menu.getCode());
                if (subList != null) {%>
            <ul class="submenu">
                <%
                    for (MenuInfo subMenu : subList) {
                        Boolean subHasRole = roleService.hasRole(subMenu.getId(), userInfo);
                        if (subHasRole || isSuper) {
                %>
                <li class="">
                    <%if (menuService.hasSubLeafMenu(subMenu.getCode())) {%>
                    <a href="#" class="dropdown-toggle">
                        <i class="menu-icon fa <%=subMenu.getIcon()%>"></i>
                            <span class="menu-text">
								<%=subMenu.getName()%>
							</span>

                        <b class="arrow fa fa-angle-down"></b>
                    </a>

                    <b class="arrow"></b>
                    <%} else {%>
                    <a href="javascript:addTabs('<%=subMenu.getCode()%>','<%=subMenu.getName()%>','<%=subMenu.getUrl()%>',true)">
                        <i class="menu-icon fa fa-caret-right"></i>
                        <%=subMenu.getName()%>
                    </a>

                    <b class="arrow"></b>
                    <%}%>

                    <%
                        List<MenuInfo> subList2 = menuService.getSubMenuList(subMenu.getCode());
                        if (subList2 != null) {
                            for (MenuInfo subMenu2 : subList2) {
                                Boolean subHasRole2 = roleService.hasRole(subMenu2.getId(), userInfo);
                                if ((subHasRole2 || isSuper) && subMenu2.getType() == MenuType.LEAF) {
                    %>
                    <ul class="submenu">
                        <li class="">
                            <a href="javascript:addTabs('<%=subMenu2.getCode()%>','<%=subMenu2.getName()%>','<%=subMenu2.getUrl()%>',true)">
                                <i class="menu-icon fa fa-caret-right"></i>
                                <%=subMenu2.getName()%>
                            </a>

                            <b class="arrow"></b>
                        </li>
                    </ul>
                    <%
                                }
                            }
                        }
                    %>
                </li>

                <%
                        }
                    }%>
            </ul>
            <%}%>
        </li>
        <%
                    }
                }
            }
        %>

    </ul>
    <!-- /.nav-list -->

    <div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
        <i id="sidebar-toggle-icon" class="ace-icon fa fa-angle-double-left ace-save-state"
           data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
    </div>
</div>
