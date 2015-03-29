package com.oldratlee.vdns;

import com.oldratlee.vdns.internal.InetAddressCacheUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author ding.lid
 * @see DnsCacheEntry
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

    /**
     * Load virtual dns config from properties file {@code vdns.properties} on classpath, then set virtual dns.
     */
    public static void configVirtualDnsByClassPathProperties() {
        configVirtualDnsByClassPathProperties("vdns.properties");
    }

    /**
     * Load virtual dns config from the specified properties file on classpath, then set virtual dns.
     *
     * @param propertiesFileName specified properties file name on classpath.
     */
    public static void configVirtualDnsByClassPathProperties(String propertiesFileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName);
        if (inputStream == null) {
            inputStream = VirtualDns.class.getClassLoader().getResourceAsStream(propertiesFileName);
        }
        if (inputStream == null) {
            throw new VirtualDnsException("Fail to find " + propertiesFileName + " on classpath!");
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

    public static List<DnsCacheEntry> getAllDnsCacheEntry() {
        try {
            return InetAddressCacheUtil.listAllVirtualDns();
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to getAllVirtualDns, cause: " + e.toString(), e);
        }
    }

    public static void clearDnsCacheEntry() {
        try {
            InetAddressCacheUtil.clearInetAddressCache();
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to clearDnsCacheEntry, cause: " + e.toString(), e);
        }
    }
}
