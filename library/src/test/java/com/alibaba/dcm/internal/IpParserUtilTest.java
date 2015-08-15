package com.alibaba.dcm.internal;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class IpParserUtilTest {
    @Test
    public void test_ip2ByteArray() throws Exception {
        assertArrayEquals(new byte[]{10, 1, 1, 1},
                IpParserUtil.ip2ByteArray("10.1.1.1"));
        assertArrayEquals(new byte[]{(byte) 192, (byte) 168, (byte) 0, 13},
                IpParserUtil.ip2ByteArray("192.168.0.13"));
        assertArrayEquals(new byte[]{10, (byte) 192, (byte) 255, 0},
                IpParserUtil.ip2ByteArray("10.192.255.0"));

        final String ip = "2404:6800:4005:80a:0:0:0:200e";
        final byte[] bytes = IpParserUtil.ip2ByteArray(ip);
        assertArrayEquals(getInetAddressByGetAllByName(ip).getAddress(), bytes);
    }

    @Test
    public void test_ip2ByteArray_ipv4_exception() throws Exception {
        // ipv4 with char
        try {
            IpParserUtil.ip2ByteArray("a.1.1.1");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }
        
        // ipv4_minus
        try {
            IpParserUtil.ip2ByteArray("-2.168.0.13");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }

        // ipv4 overflow
        try {
            IpParserUtil.ip2ByteArray("1.1.1.256");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }

        // ipv4 too long
        try {
            IpParserUtil.ip2ByteArray("192.168.0.13.1");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }
    }

    @Test
    public void test_ip2ByteArray_ipv6_exception() throws Exception {
        // ipv4 with char
        try {
            IpParserUtil.ip2ByteArray("2404:6800:4005:80a:0:0:0:200z");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }
        
        // ipv4_minus
        try {
            IpParserUtil.ip2ByteArray("-2404:6800:4005:80a:0:0:0:200e");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }

        // ipv4 too long
        try {
            IpParserUtil.ip2ByteArray("2404:6800:4005:80a:0:0:0:200:123");
            fail();
        } catch (IllegalArgumentException expected) {
            expected.printStackTrace();
        }
    }

    static InetAddress getInetAddressByGetAllByName(String ip) throws Exception {
        final InetAddress[] addresses = InetAddress.getAllByName(ip);
        assertEquals(1, addresses.length);
        return addresses[0];
    }
}
