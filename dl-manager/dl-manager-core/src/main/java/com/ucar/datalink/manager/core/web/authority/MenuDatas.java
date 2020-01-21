package com.ucar.datalink.manager.core.web.authority;

/**
 * Created by qq.shi01 on 2017/4/21.
 */
public class MenuDatas {

    private static final String ROLE_COMMON = "ORDINARY";

    private static final String ROLE_MAGAGER = "SUPER";

    private static final String ROLE_ALL = ROLE_COMMON + "," + ROLE_MAGAGER;

    public static final Object[][] menuArr = new Object[][]{
            //code, name, parentCode, type, url, icon, role
            //---------menu1----------
            {"001000000", "集群管理", "000000000", "node", null, "fa-desktop",ROLE_ALL},

            {"001001000", "分组管理", "001000000", "leaf", "/group/groupList",null, ROLE_MAGAGER},
            //子页面
            {"001001001", "跳转分组列表", "001001000", "action", "/group/initGroup",null, ROLE_MAGAGER},
            {"001001002", "跳转新增页面", "001001000", "action", "/group/toAdd",null, ROLE_MAGAGER},
            {"001001003", "分组新增", "001001000", "action", "/group/doAdd",null, ROLE_MAGAGER},
            {"001001004", "跳转修改页面", "001001000", "action", "/group/toEdit",null, ROLE_MAGAGER},
            {"001001005", "分组修改", "001001000", "action", "/group/doEdit",null, ROLE_MAGAGER},
            {"001001006", "分组删除", "001001000", "action", "/group/delete",null, ROLE_MAGAGER},

            {"001002000", "机器管理", "001000000", "leaf", "/worker/workerList",null, ROLE_MAGAGER},
            //子页面
            {"001002001", "跳转机器列表", "001002000", "action", "/worker/initWorker",null, ROLE_MAGAGER},
            {"001002002", "跳转新增页面", "001002000", "action", "/worker/toAdd",null, ROLE_MAGAGER},
            {"001002003", "机器新增", "001002000", "action", "/worker/doAdd",null, ROLE_MAGAGER},
            {"001002004", "跳转修改页面", "001002000", "action", "/worker/toEdit",null, ROLE_MAGAGER},
            {"001002005", "机器修改", "001002000", "action", "/worker/doEdit",null, ROLE_MAGAGER},
            {"001002006", "机器删除", "001002000", "action", "/worker/delete",null, ROLE_MAGAGER},

            //---------menu2----------
            {"002000000", "介质管理", "000000000", "node", null, "fa-android",ROLE_ALL},

            {"002001000", "RDBMS", "002000000", "leaf", "/mediaSource/mediaSourceList",null, ROLE_MAGAGER},
            //子页面
            {"002001001", "跳转列表", "002001000", "action", "/mediaSource/initMediaSource",null, ROLE_MAGAGER},
            {"002001002", "跳转新增页面", "002001000", "action", "/mediaSource/toAdd",null, ROLE_MAGAGER},
            {"002001003", "新增", "002001000", "action", "/mediaSource/doAdd",null, ROLE_MAGAGER},
            {"002001004", "跳转修改页面", "002001000", "action", "/mediaSource/toEdit",null, ROLE_MAGAGER},
            {"002001005", "修改", "002001000", "action", "/mediaSource/doEdit",null, ROLE_MAGAGER},
            {"002001006", "删除", "002001000", "action", "/mediaSource/doDelete",null, ROLE_MAGAGER},
            {"002001007", "验证数据源", "002001000", "action", "/mediaSource/checkDbConnection",null, ROLE_MAGAGER},
            {"002001009", "跳转自动新增数据源页面", "002001000", "action", "/mediaSource/toAddQuick",null, ROLE_MAGAGER},
            {"002001010", "自动新增数据源", "002001000", "action", "/mediaSource/doAddQuick",null, ROLE_MAGAGER},

            {"002002000", "HBase", "002000000", "leaf", "",null, ROLE_MAGAGER},
            {"002003000", "ElasticSearch", "002000000", "leaf", "",null, ROLE_MAGAGER},

            //---------menu3----------
            {"003000000", "同步管理", "000000000", "node", null, "fa-building",ROLE_ALL},
            {"003001000", "同步申请", "003000000", "leaf", "", null, ROLE_MAGAGER},
            {"003002000", "Schema管理", "003000000", "leaf", "",null, ROLE_MAGAGER},
            {"003003000", "同步检测", "003000000", "leaf", "",null, ROLE_MAGAGER},

            //---------menu4----------
            {"004000000", "增量任务", "000000000", "node", null, "fa-list-alt",ROLE_ALL},

            {"004010000", "Task管理", "004000000", "leaf", null,null, ROLE_MAGAGER},
            {"004010100", "MysqlTask", "004010000", "leaf", "/task/mysqlTaskList",null, ROLE_MAGAGER},
            //子页面
            {"004010101", "跳转新增页面", "004010100", "action", "/task/toAddMysqlTask",null, ROLE_MAGAGER},
            {"004010102", "新增", "004010100", "action", "/task/doAddMysqlTask",null, ROLE_MAGAGER},
            {"004010103", "跳转修改页面", "004010100", "action", "/task/toUpdateMysqlTask",null, ROLE_MAGAGER},
            {"004010104", "修改", "004010100", "action", "/task/doUpdateMysqlTask",null, ROLE_MAGAGER},
            {"004010105", "删除", "004010100", "action", "/task/deleteMysqlTask",null, ROLE_MAGAGER},
            {"004010106", "暂停运行", "004010100", "action", "/task/pauseMysqlTask",null, ROLE_MAGAGER},
            {"004010107", "恢复运行", "004010100", "action", "/task/resumeMysqlTask",null, ROLE_MAGAGER},
            {"004010108", "重启", "004010100", "action", "/task/toRestartMysqlTask",null, ROLE_MAGAGER},
            {"004010109", "执行重启", "004010100", "action", "/task/doRestartMysqlTask",null, ROLE_MAGAGER},
            {"004010110", "跳转MysqlTask列表", "004010100", "action", "/task/mysqlTaskDatas",null, ROLE_MAGAGER},

            {"004010200", "HBaseTask", "004010000", "leaf", "",null, ROLE_MAGAGER},
            {"004010300", "ElasticSearchTask", "004010000", "leaf", "",null, ROLE_MAGAGER},

            {"004020000", "映射管理", "004000000", "leaf", "/mediaMapping/mediaSourceList",null, ROLE_MAGAGER},
            //子页面
            {"004020100", "跳转列表", "004020000", "action", "/mediaMapping/initMediaMapping",null, ROLE_MAGAGER},
            {"004020200", "跳转新增页面", "004020000", "action", "/mediaMapping/toAdd",null, ROLE_MAGAGER},
            {"004020300", "新增", "004020000", "action", "/mediaMapping/doAdd",null, ROLE_MAGAGER},
            {"004020400", "跳转修改页面", "004020000", "action", "/mediaMapping/toEdit",null, ROLE_MAGAGER},
            {"004020500", "修改", "004020000", "action", "/mediaMapping/doEdit",null, ROLE_MAGAGER},
            {"004020600", "删除", "004020000", "action", "/mediaMapping/doDelete",null, ROLE_MAGAGER},

            //---------menu5----------
            {"005000000", "全量任务", "000000000", "node", null, "fa-calculator",ROLE_ALL},

            //---------menu6----------
            {"006000000", "监控管理", "000000000", "node", null, "fa-bell-o",ROLE_ALL},

            //---------menu7----------
            {"007000000", "系统管理", "000000000", "node", null, "fa-coffee",ROLE_ALL},

            {"007001000", "用户管理", "007000000", "leaf", "/user/userList",null, ROLE_MAGAGER},
            //子页面
            {"007001001", "跳转列表", "007001000", "action", "/user/initUser",null, ROLE_MAGAGER},
            {"007001002", "跳转新增页面", "007001000", "action", "/user/toAdd",null, ROLE_MAGAGER},
            {"007001003", "新增", "007001000", "action", "/user/doAdd",null, ROLE_MAGAGER},
            {"007001004", "跳转修改页面", "007001000", "action", "/user/toEdit",null, ROLE_MAGAGER},
            {"007001005", "修改", "007001000", "action", "/user/doEdit",null, ROLE_MAGAGER},
            {"007001006", "删除", "007001000", "action", "/user/doDelete",null, ROLE_MAGAGER},

            {"007002000", "菜单管理", "007000000", "leaf", "/menu/menuList",null, ROLE_MAGAGER},
            //子页面
            {"007002001", "跳转列表", "007002000", "action", "/menu/initMenu",null, ROLE_MAGAGER},
            {"007002002", "跳转新增页面", "007002000", "action", "/menu/toAdd",null, ROLE_MAGAGER},
            {"007002003", "新增", "007002000", "action", "/menu/doAdd",null, ROLE_MAGAGER},
            {"007002004", "跳转修改页面", "007002000", "action", "/menu/toEdit",null, ROLE_MAGAGER},
            {"007002005", "修改", "007002000", "action", "/menu/doEdit",null, ROLE_MAGAGER},
            {"007002006", "删除", "007002000", "action", "/menu/doDelete",null, ROLE_MAGAGER}

    };
}
