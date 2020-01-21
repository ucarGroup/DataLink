package com.ucar.datalink.manager.core.web.util;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 23/11/2017.
 */
public enum CFCenterSddladminEnum {

    UCAR(BusinessLine.UCAR.toString(), new HashMap<String, String>(){
        {
            put(DEPLOY_XN2,  CFCENT_PN+"_07ec3241f7ab7ff39b5645bb9784f488_http://gaeatest.10101111.com");
            put(DEPLOY_TEST,  CFCENT_PN+"_07ec3241f7ab7ff39b5645bb9784f488_http://gaeatest.10101111.com");
            put(DEPLOY_TEST2, CFCENT_PN+"_5ac96ba5cbced3957b30966e4856da84_http://gaeatest02.10101111.com");
            put(DEPLOY_TEST3, CFCENT_PN+"_a73b57fa8a24ed984dac6c2953f630c3_http://gaeatest03.10101111.com");
            put(DEPLOY_PRE,   CFCENT_PN+"_16f0969bf383a47a3e651766d5a69677_http://gaeapre.10101111.com");
            put(DEPLOY_PROD,  CFCENT_PN+"_8fed9cd189c9ae8fcbee3612edfae616_http://gaea.10101111.com");
        }
    }),
    ZUCHE(BusinessLine.ZUCHE.toString(), new HashMap<String, String>(){
        {
            put(DEPLOY_XN2,  CFCENT_PN+"_3b96190fca86966c1805e0340f7d5677_http://gaeatest.10101111.com");
            put(DEPLOY_TEST,  CFCENT_PN+"_3b96190fca86966c1805e0340f7d5677_http://gaeatest.10101111.com");
            put(DEPLOY_TEST2, CFCENT_PN+"__http://gaeatest02.10101111.com");
            put(DEPLOY_TEST3, CFCENT_PN+"__http://gaeatest03.10101111.com");
            put(DEPLOY_PRE,   CFCENT_PN+"_ee495046a1df35f657d7afe7f7336fd6_http://gaeapre.10101111.com");
            put(DEPLOY_PROD,  CFCENT_PN+"_723a9e1f5460f2b6a7dffc1a007bec78_http://gaea.10101111.com");
        }
    }),
    FCAR(BusinessLine.FCAR.toString(), new HashMap<String, String>(){
        {
            put(DEPLOY_XN2,  CFCENT_PN+"_91b719c1dbdaed199ca26085744be1b0_http://gaeatest.10101111.com");
            put(DEPLOY_TEST,  CFCENT_PN+"_91b719c1dbdaed199ca26085744be1b0_http://gaeatest.10101111.com");
            put(DEPLOY_TEST2, CFCENT_PN+"_3f577cc3e633dc1182cdee1a8cd321be_http://gaeatest02.10101111.com");
            put(DEPLOY_TEST3, CFCENT_PN+"_65efbcae552dd997d1063a022a648544_http://gaeatest03.10101111.com");
            put(DEPLOY_PRE,   CFCENT_PN+"_b6d0054a4df7aa8246e7a85f7f3b4d24_http://gaeapre.10101111.com");
            put(DEPLOY_PROD,  CFCENT_PN+"_f881f021a67688338abff95399bf7083_http://gaea.10101111.com");
        }
    }),
    MMC(BusinessLine.MMC.toString(), new HashMap<String, String>(){
        {
            put(DEPLOY_XN2,  CFCENT_PN+"_64dada7a59825aeff10ed3bfbec34a77_http://gaeatest.10101111.com");
            put(DEPLOY_TEST,  CFCENT_PN+"_64dada7a59825aeff10ed3bfbec34a77_http://gaeatest.10101111.com");
            put(DEPLOY_TEST2, CFCENT_PN+"__http://gaeatest02.10101111.com");
            put(DEPLOY_TEST3, CFCENT_PN+"__http://gaeatest03.10101111.com");
            put(DEPLOY_PRE,   CFCENT_PN+"_6e4ab7323f72c9973fcab52d5375659e_http://gaeapre.10101111.com");
            put(DEPLOY_PROD,  CFCENT_PN+"_dfee4629ae8255dbe7ae399129c30bef_http://gaea.10101111.com");
        }
    }),
    LUCKY(BusinessLine.LUCKY.toString(), new HashMap<String, String>(){
        {
            put(DEPLOY_XN2,  CFCENT_PN+"_30bb13489c3eb46141c7cd1b1efdc83e_http://gaeatest.10101111.com");
            put(DEPLOY_TEST,  CFCENT_PN+"_30bb13489c3eb46141c7cd1b1efdc83e_http://gaeatest.10101111.com");
            put(DEPLOY_TEST2, CFCENT_PN+"_08aefe3bcef3ff1f3d068a143e60f302_http://gaeatest02.10101111.com");
            put(DEPLOY_TEST3, CFCENT_PN+"__http://gaeatest03.10101111.com");
            put(DEPLOY_PRE,   CFCENT_PN+"_d6e020a31404dac7815d8bde7590eb89_http://gaeapre.10101111.com");
            put(DEPLOY_PROD,  CFCENT_PN+"_d6e020a31404dac7815d8bde7590eb89_http://gaea.10101111.com");
        }
    });


    private String businessline;           // UCAR,ZUCHE,FCAR,MMC,LUCKY
    private Map<String, String> cfCentConf;// key:'test'\'test2'\'test3'\'pre'\'prod',
    // value:"sddladmin_key_serverDomain"

    private static final String DEPLOY_XN2   = "xn2";
    private static final String DEPLOY_TEST  = "test";
    private static final String DEPLOY_TEST2 = "test2";
    private static final String DEPLOY_TEST3 = "test3";
    private static final String DEPLOY_PRE   = "pre";
    private static final String DEPLOY_PROD  = "prod";

    private static final String CFCENT_PN    = "sddladmin";

    CFCenterSddladminEnum(String businessline, Map<String, String> cfCentConf) {
        this.businessline = businessline;
        this.cfCentConf = cfCentConf;
    }

    /**
     * @Description: 获取sddladmin项目对应的projectName（sddladmin）,key,serverDomain
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 7:44 PM 24/11/2017
     */
    public static List<String> getSddladminCfConf(String businessline, String currentDev) {
        for (CFCenterSddladminEnum cfEnumMaster : CFCenterSddladminEnum.values()) {
            if (cfEnumMaster.getBusinessline().equals(businessline)) {
                Map<String, String> cfEnumMasterValue = cfEnumMaster.getCfCentConf();
                for (Map.Entry<String, String> entry : cfEnumMasterValue.entrySet()) {
                    if (entry.getKey().equals(currentDev)) {
                        return Lists.newArrayList(entry.getValue().split("_"));
                    }
                }
            }
        }

        return null;
    }


    public String getBusinessline() {
        return businessline;
    }

    public void setBusinessline(String businessline) {
        this.businessline = businessline;
    }

    public Map<String, String> getCfCentConf() {
        return cfCentConf;
    }

    public void setCfCentConf(Map<String, String> cfCentConf) {
        this.cfCentConf = cfCentConf;
    }


    public enum BusinessLine {
        UCAR,ZUCHE,FCAR,MMC,LUCKY;
    }

}
