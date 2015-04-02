package com.alibaba.dcm.internal;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

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
}
