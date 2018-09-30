package com.ucar.datalink.contract.log.rdbms;

import java.util.Comparator;

/**
 * 按照EventColumn的index进行排序.
 * 
 * @author lubiao
 */
public class EventColumnIndexComparable implements Comparator<EventColumn> {

    public int compare(EventColumn o1, EventColumn o2) {
        if (o1.getIndex() < o2.getIndex()) {
            return -1;
        } else if (o1.getIndex() == o2.getIndex()) {
            return 0;
        } else {
            return 1;
        }
    }

}
