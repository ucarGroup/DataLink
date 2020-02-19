package com.ucar.datalink.flinker.test;

import com.ucar.datalink.flinker.core.admin.DataxAdminLauncher;

public class FlinkerAdminBootstrapTest {

    public static void main(String[] args) {
        String flinker_home = "E:\\ucar\\UCARDATALINK_branches\\datax_2_datalink\\target\\dl-flinker";
        System.setProperty("datax.home",flinker_home);
        DataxAdminLauncher.main(args);
    }

}
