package com.ucar.datalink.domain.media;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/3/24.
 */
public class ModeUtilsTest {

    @Test
    public void testIndexIgnoreCase() {
        String s = "table_[0000-0031]";
        MediaInfo.ModeValue value = ModeUtils.parseMode(s);
        for (int i = 0; i < 32; i++) {
            Assert.assertEquals("table_" + i, value.getMultiValue().get(i));
            System.out.println(value.getMultiValue().get(i));
        }
    }

    @Test
    public void testParse() {
        MediaInfo.ModeValue modeValue = ModeUtils.parseMode("tcar_order_m[0-3]");
        if (modeValue.getMode().isMulti()) {
            System.out.println(modeValue.getMultiValue().get(0));
        }

        modeValue = ModeUtils.parseMode("t_scd_order_${yyyy}");
        Assert.assertEquals(true, modeValue.getMode().isYearly());

        modeValue = ModeUtils.parseMode("t_scd_order_${yyyyMM}");
        Assert.assertEquals(true, modeValue.getMode().isMonthly());
    }

    @Test
    public void testIsYearlyMatch() {
        Assert.assertEquals(true, ModeUtils.isYearlyMatch("t_scd_order_${yyyy}", "t_scd_order_2017"));
        Assert.assertEquals(false, ModeUtils.isYearlyMatch("t_scd_order${yyyy}", "t_scd_order_2017"));
        Assert.assertEquals(false, ModeUtils.isYearlyMatch("t_scd_order11${yyyy}", "t_scd_order_2017"));
        Assert.assertEquals(true, ModeUtils.isYearlyMatch("t_scd_order_${yyyy}", "t_scd_order_0017"));
        Assert.assertEquals(true, ModeUtils.isYearlyMatch("t_scd_order_${yyyy}", "t_scd_order_0000"));
        Assert.assertEquals(false, ModeUtils.isYearlyMatch("t_scd_order_${yyyy}", "t_scd_order_yyyy"));
    }

    @Test
    public void testIsMonthlyMatch() {
        Assert.assertEquals(true, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_201712"));
        Assert.assertEquals(true, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_2017-12"));
        Assert.assertEquals(true, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_2017/12"));
        Assert.assertEquals(true, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_2017.12"));
        Assert.assertEquals(false, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_2017_12"));
        Assert.assertEquals(false, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_ordervv_201712"));
        Assert.assertEquals(false, ModeUtils.isMonthlyMatch("t_scd_orderff_${yyyyMM}", "t_scd_order_201712"));
        Assert.assertEquals(false, ModeUtils.isMonthlyMatch("t_scd_order_${yyyyMM}", "t_scd_order_2017ii"));
    }

    @Test
    public void testGetYearlyPrefix() {
        String s = ModeUtils.getYearlyPrefix("t_scd_order_${yyyy}");
        Assert.assertEquals("t_scd_order_", s);
    }

    @Test
    public void testGetMonthlyPrefix() {
        String s = ModeUtils.getMonthlyPrefix("t_scd_order_${yyyyMM}");
        Assert.assertEquals("t_scd_order_", s);
    }

    @Test
    public void testGroup() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(1);
        list.add(2);
        list.add(2);
        list.add(3);
        System.out.println(list.stream().collect(Collectors.groupingBy(Integer::intValue)).size());

        int min = list.stream().collect(Collectors.groupingBy(Integer::intValue, Collectors.counting())).values().stream().mapToInt(i -> i.intValue()).summaryStatistics().getMin();
        System.out.println(min);
    }
}
