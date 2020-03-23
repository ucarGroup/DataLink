package com.ucar.datalink.worker.core.boot;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2020/2/23.
 */
public class Test {
    public static void main(String args[]){
        int[] array1 = new int[1000000];
        List<Integer> array2 = new LinkedList<>();

        for(int i=0;i<array1.length;i++){
            array1[i] = i;
        }

        for(int i=0;i<array1.length;i++){
            array2.add(i);
        }

        long time1 = System.currentTimeMillis();
        int sum = 0;
        for (int i =0;i<array1.length;i++){
            sum = array1[i];
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);

        long time3 = System.currentTimeMillis();
        int sum2 = 0;
        Iterator iterator = array2.iterator();
        while(iterator.hasNext()){
            sum2 = (Integer)iterator.next();
        }
        long time4 = System.currentTimeMillis();
        System.out.println(time4 - time3);

    }
}
