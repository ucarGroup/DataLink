package com.ucar.datalink.worker.core.boot;

/**
 * Created by lubiao on 2017/1/17.
 */
public class WorkerBootStrapTest {

    public static void main(String[] args){
        WorkerBootStrap bootStrap = new WorkerBootStrap();
        bootStrap.boot(args);
    }
}
