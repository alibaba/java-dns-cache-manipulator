package com.alibaba.dcm.internal;

import sun.net.util.IPAddressUtil;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class IpParserUtil {
    private static final String INVALID_IPV_6_ADDRESS = ": invalid IPv6 address";
    private static final String INVALID_IP_ADDRESS = ": invalid IP address";

    /**
     * source code is dug from {@link java.net.InetAddress#getAllByName(java.lang.String, java.net.InetAddress)}
     */
    static byte[] ip2ByteArray(String ip) {

        boolean ipv6Expected = false;
        if (ip.charAt(0) == '[') {
            // This is supposed to be an IPv6 literal
            if (ip.length() > 2 && ip.charAt(ip.length() - 1) == ']') {
                ip = ip.substring(1, ip.length() - 1);
                ipv6Expected = true;
            } else {
                // This was supposed to be a IPv6 address, but it's not!
                throw new IllegalArgumentException(ip + INVALID_IPV_6_ADDRESS);
            }
        }

        if (Character.digit(ip.charAt(0), 16) != -1 || (ip.charAt(0) == ':')) {
            // see if it is IPv4 address
            byte[] address = IPAddressUtil.textToNumericFormatV4(ip);
            if (address != null) return address;

            // see if it is IPv6 address
            // Check if a numeric or string zone id is present
            address = IPAddressUtil.textToNumericFormatV6(ip);
            if (address != null) return address;


            if (ipv6Expected) {
                throw new IllegalArgumentException(ip + INVALID_IPV_6_ADDRESS);
            } else {
                throw new IllegalArgumentException(ip + INVALID_IP_ADDRESS);
            }
        } else {
            throw new IllegalArgumentException(ip + INVALID_IP_ADDRESS);
        }
    }
}
