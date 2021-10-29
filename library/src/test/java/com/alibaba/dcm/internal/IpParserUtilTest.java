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
    public void test_ipv42ByteArray() throws Exception {
        assertArrayEquals(new byte[]{(byte) 192, (byte) 168, (byte) 0, 13},
                IpParserUtil.ip2ByteArray("192.168.0.13"));
        assertArrayEquals(new byte[]{10, (byte) 192, (byte) 255, 0},
                IpParserUtil.ip2ByteArray("10.192.255.0"));

        final String ip = "10.1.1.1";
        final byte[] actualIpBytes = IpParserUtil.ip2ByteArray(ip);
        assertArrayEquals(new byte[]{10, 1, 1, 1}, actualIpBytes);
        assertArrayEquals(getIpByGetAllByName(ip), actualIpBytes);
    }

    @Test
    public void test_ipv62ByteArray() throws Exception {
        final String ip = "2404:6800:4005:80a:0:0:0:200e";
        final byte[] bytes = IpParserUtil.ip2ByteArray(ip);
        assertArrayEquals(getIpByGetAllByName(ip), bytes);
    }

    private static byte[] getIpByGetAllByName(String ip) throws Exception {
        final InetAddress[] addresses = InetAddress.getAllByName(ip);
        assertEquals(1, addresses.length);
        return addresses[0].getAddress();
    }

    public static final String INVALID_IP_ADDRESS = ": invalid IP address";

    @Test
    public void test_ip2ByteArray_ipv4_with_char_exception() {
        final String ip = "a.1.1.1";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv4_minus_exception() {
        final String ip = "-2.168.0.13";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv4_overflow_exception() {
        final String ip = "1.1.1.256";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv4_too_long_exception() {
        final String ip = "192.168.0.13.1";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv6_with_char_exception() {
        final String ip = "2404:6800:4005:80a:0:0:0:200z";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv6_minus_exception() {
        final String ip = "-2404:6800:4005:80a:0:0:0:200e";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }

    @Test
    public void test_ip2ByteArray_ipv6_too_long_exception() {
        final String ip = "2404:6800:4005:80a:0:0:0:200:123";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ip + INVALID_IP_ADDRESS);
        IpParserUtil.ip2ByteArray(ip);
    }
}
