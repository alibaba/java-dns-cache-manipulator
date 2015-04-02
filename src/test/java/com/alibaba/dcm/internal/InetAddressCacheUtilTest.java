package com.alibaba.dcm.internal;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author ding.lid
 */
public class InetAddressCacheUtilTest {
    @Test
    public void test_ip2ByteArray() throws Exception {
        assertArrayEquals(new byte[]{1, 1, 1, 1},
                InetAddressCacheUtil.ip2ByteArray("1.1.1.1"));
        assertArrayEquals(new byte[]{10, (byte) 192, (byte) 255, 0},
                InetAddressCacheUtil.ip2ByteArray("10.192.255.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void test_ip2ByteArray_NumberFormatException() throws Exception {
        InetAddressCacheUtil.ip2ByteArray("a.1.1.1");
    }

    @Test
    public void test_ip2ByteArray_exception() throws Exception {
        try {
            InetAddressCacheUtil.ip2ByteArray("-1.1.1.1");
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("-1 is not a byte!", expected.getMessage());
        }

        try {
            InetAddressCacheUtil.ip2ByteArray("1.1.1.256");
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("256 is not a byte!", expected.getMessage());
        }
    }
}
