package com.oldratlee.vdns;

import com.oldratlee.vdns.internal.InetAddressCacheUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A simple lib for setting dns (in fact dns cache) programmatically.
 *
 * @author ding.lid
 * @see DnsCacheEntry
 * @see VirtualDnsException
 */
public class VirtualDns {
    private static final long NEVER_EXPIRATION = Long.MAX_VALUE;

    /**
     * Set a <b>never expired</b> dns cache entry
     *
     * @param host host
     * @param ips  ips
     * @see VirtualDns#setVirtualDns(long, java.lang.String, java.lang.String...)
     */
    public static void setVirtualDns(String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, NEVER_EXPIRATION);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to setVirtualDns, cause: " + e.toString(), e);
        }
    }

    /**
     * Set a dns cache entry.
     *
     * @param expireMillis expire time in milliseconds.
     * @param host         host
     * @param ips          ips
     */
    public static void setVirtualDns(long expireMillis, String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, System.currentTimeMillis() + expireMillis);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to setVirtualDns, cause: " + e.toString(), e);
        }
    }

    /**
     * Set dns cache entries by properties
     *
     * @param properties input properties. eg. {@code www.example.com=42.42.42.42}
     */
    public static void setVirtualDns(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String host = (String) entry.getKey();
            final String ip = (String) entry.getValue();

            setVirtualDns(host, ip);
        }
    }

    /**
     * Load virtual dns config from properties file {@code vdns.properties} on classpath, then set to dns cache.
     *
     * @see VirtualDns#setVirtualDns(java.util.Properties)
     * @see VirtualDns#configVirtualDnsByClassPathProperties(java.lang.String)
     */
    public static void configVirtualDnsByClassPathProperties() {
        configVirtualDnsByClassPathProperties("vdns.properties");
    }

    /**
     * Load virtual dns config from the specified properties file on classpath, then set dns cache.
     *
     * @param propertiesFileName specified properties file name on classpath.
     * @see com.oldratlee.vdns.VirtualDns#setVirtualDns(java.util.Properties)
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

    /**
     * Get all dns cache entries.
     *
     * @return dns cache entries
     */
    public static List<DnsCacheEntry> getAllDnsCacheEntries() {
        try {
            return InetAddressCacheUtil.listAllVirtualDns();
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to getAllVirtualDns, cause: " + e.toString(), e);
        }
    }

    /**
     * Remove dns cache entry, cause lookup dns server for host after.
     *
     * @param host host
     * @see VirtualDns#clearDnsCacheEntry
     */
    public static void removeVirtualDns(String host) {
        try {
            InetAddressCacheUtil.removeInetAddressCache(host);
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to removeVirtualDns, cause: " + e.toString(), e);
        }
    }

    /**
     * Clear all dns cache entries, cause lookup dns server for all host after.
     */
    public static void clearDnsCacheEntry() {
        try {
            InetAddressCacheUtil.clearInetAddressCache();
        } catch (Exception e) {
            throw new VirtualDnsException("Fail to clearDnsCacheEntry, cause: " + e.toString(), e);
        }
    }
}
