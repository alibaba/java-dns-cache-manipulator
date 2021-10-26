package com.alibaba.dcm.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.InetAddress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class IpParserUtilTest {

    @Rule
    @SuppressWarnings("deprecation")
    public final ExpectedException thrown = ExpectedException.none();

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
    public void test_ip2ByteArray_ipv4_with_char_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("a.1.1.1: invalid IP address");
        IpParserUtil.ip2ByteArray("a.1.1.1");

    }

    @Test
    public void test_ip2ByteArray_ipv4_minus_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("-2.168.0.13: invalid IP address");
        IpParserUtil.ip2ByteArray("-2.168.0.13");
    }

    @Test
    public void test_ip2ByteArray_ipv4_overflow_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("1.1.1.256: invalid IP address");
        IpParserUtil.ip2ByteArray("1.1.1.256");
    }

    @Test
    public void test_ip2ByteArray_ipv4_too_long_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("192.168.0.13.1: invalid IP address");
        IpParserUtil.ip2ByteArray("192.168.0.13.1");
    }

    @Test
    public void test_ip2ByteArray_ipv6_with_char_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("2404:6800:4005:80a:0:0:0:200z: invalid IP address");
        IpParserUtil.ip2ByteArray("2404:6800:4005:80a:0:0:0:200z");
    }

    @Test
    public void test_ip2ByteArray_ipv6_minus_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("-2404:6800:4005:80a:0:0:0:200e: invalid IP address");
        IpParserUtil.ip2ByteArray("-2404:6800:4005:80a:0:0:0:200e");
    }

    @Test
    public void test_ip2ByteArray_ipv6_too_long_exception(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("2404:6800:4005:80a:0:0:0:200:123: invalid IP address");
        IpParserUtil.ip2ByteArray("2404:6800:4005:80a:0:0:0:200:123");
    }

    private static InetAddress getInetAddressByGetAllByName(String ip) throws Exception {
        final InetAddress[] addresses = InetAddress.getAllByName(ip);
        assertEquals(1, addresses.length);
        return addresses[0];
    }
}
