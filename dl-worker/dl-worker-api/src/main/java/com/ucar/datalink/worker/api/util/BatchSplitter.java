package com.ucar.datalink.worker.api.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2017/6/21.
 */
public class BatchSplitter {

    /**
     * 将一个list拆分为多个list，每个list的元素个数为batchSize
     *
     */
    public static <T> List<List<T>> splitForBatch(List<T> datas, int batchSize) {
        List<List<T>> result = new ArrayList<>();

        if (batchSize >= datas.size()) {
            result.add(datas);
            return result;
        } else {
            LinkedList<T> tempList = new LinkedList<>();
            for (T item : datas) {
                tempList.add(item);
                if (tempList.size() % batchSize == 0) {
                    result.add(tempList);
                    tempList = new LinkedList<>();
                }
            }
            if (!tempList.isEmpty()) {
                result.add(tempList);
            }

            return result;
        }
    }

    public static void main(String args[]) {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        List<List<Integer>> result = splitForBatch(list, 101);
        System.out.println(result.size());
        for (List<Integer> i : result) {
            System.out.println(i);
        }

        List<List<Integer>> result2 = splitForBatch(list, 10);
        System.out.println(result2.size());
        for (List<Integer> i : result2) {
            System.out.println(i);
        }

        List<List<Integer>> result3 = splitForBatch(list, 3);
        System.out.println(result3.size());
        for (List<Integer> i : result3) {
            System.out.println(i);
        }
    }
}
