package com.ucar.datalink.flinker.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Json {

    public static String file() throws IOException {
        String s = FileUtils.readFileToString(new File("E:\\ucar\\UCARDATALINK_branches\\datax_2_datalink\\dl-flinker\\aa.txt"), "UTF-8");
        return s;

    }
}
