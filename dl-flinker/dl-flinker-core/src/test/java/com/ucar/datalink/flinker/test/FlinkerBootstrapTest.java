package com.ucar.datalink.flinker.test;

import com.ucar.datalink.flinker.core.Engine;

public class FlinkerBootstrapTest {

    public static void main(String[] args) throws Exception {
        String flinker_home = "E:\\ucar\\UCARDATALINK_branches\\datax_2_datalink\\target\\dl-flinker";
        String log_back_path = flinker_home+"\\conf\\logback.xml";
        String job_id = "10777";
        job_id = "10786";
        String exec_id = "1028835";
        String job_location = "xx.json";
        System.setProperty("datax.home" ,flinker_home);
        System.setProperty("logback.configurationFile", log_back_path);

        String[] str = {
                "-mode","standalone","-jobid",job_id,"-executeId",exec_id,"-job",job_location,"-timingJobId","-1","-jqeid","-1"
        };
        Engine.main(str);
    }

}
