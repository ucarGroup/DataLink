package com.ucar.datalink.common.utils;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.ucar.datalink.common.utils.StrUtil.*;

public class StrUtilTest {

    @Test
    public void testStringify() {
        assertEquals("1B", stringify(1));
        assertEquals("1.00KB", stringify(1024));
        assertEquals("1.00MB", stringify(1048576));
        assertEquals("1.00GB", stringify(1073741824));
        assertEquals("1.00TB", stringify(1099511627776L));
    }

    @Test
    public void testReplaceVariable() {
        assertEquals("foo\\$foo\\$", replaceVariable("foo\\$foo\\$"));
    }

    @Test
    public void testCompressMiddle() {
        assertEquals("3", compressMiddle("3", 1, 9));
        assertEquals("a...c", compressMiddle("a,b,c", 1, 1));
    }
}
