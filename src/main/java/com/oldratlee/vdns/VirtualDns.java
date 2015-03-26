package com.oldratlee.vdns;

import com.oldratlee.vdns.internal.InetAddressCacheUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author ding.lid
 * @see Host
 * @see VirtualDnsException
 */
public class VirtualDns {
    private static final long NEVER_EXPIRATION = Long.MAX_VALUE;

    public static void setVirtualDns(String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, NEVER_EXPIRATION);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to setVirtualDns, cause: " + e.toString(), e);
        }
    }

    public static void setVirtualDns(long expireMillis, String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, System.currentTimeMillis() + expireMillis);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to setVirtualDns, cause: " + e.toString(), e);
        }
    }

    public static void setVirtualDns(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String host = (String) entry.getKey();
            final String ip = (String) entry.getValue();

            setVirtualDns(host, ip);
        }
    }

    public static void configVirtualDnsByClassPathProperties() {
        configVirtualDnsByClassPathProperties("vdns.properties");
    }

    public static void configVirtualDnsByClassPathProperties(String propertiesFileOnClassPath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileOnClassPath);
        if (inputStream == null) {
            inputStream = VirtualDns.class.getClassLoader().getResourceAsStream(propertiesFileOnClassPath);
        }
        if (inputStream == null) {
            throw new VirtualDnsException("Fail to find " + propertiesFileOnClassPath + " on classpath!");
        }

        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            setVirtualDns(properties);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to configVirtualDnsByClassPathProperties, cause: " + e.toString(), e);
        }
    }

    public static List<Host> getAllVirtualDns() {
        try {
            return InetAddressCacheUtil.listAllVirtualDns();
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to getAllVirtualDns, cause: " + e.toString(), e);
        }
    }
}
