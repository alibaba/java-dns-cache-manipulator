package com.oldratlee.dcm;

import com.oldratlee.dcm.internal.InetAddressCacheUtil;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A simple lib for setting dns (in fact dns cache) programmatically.
 *
 * @author ding.lid
 * @see DnsCacheEntry
 * @see DnsCacheManipulatorException
 */
public class DnsCacheManipulator {
    private static final long NEVER_EXPIRATION = Long.MAX_VALUE;

    /**
     * Set a <b>never expired</b> dns cache entry
     *
     * @param host host
     * @param ips  ips
     * @see DnsCacheManipulator#setDnsCache(long, java.lang.String, java.lang.String...)
     */
    public static void setDnsCache(String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, NEVER_EXPIRATION);
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to setDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Set a dns cache entry.
     *
     * @param expireMillis expire time in milliseconds.
     * @param host         host
     * @param ips          ips
     */
    public static void setDnsCache(long expireMillis, String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, System.currentTimeMillis() + expireMillis);
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to setDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Set dns cache entries by properties
     *
     * @param properties input properties. eg. {@code www.example.com=42.42.42.42}
     */
    public static void setDnsCache(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String host = (String) entry.getKey();
            final String ip = (String) entry.getValue();

            setDnsCache(host, ip);
        }
    }

    /**
     * Load dns config from properties file {@code dns-cache.properties} on classpath, then set to dns cache.
     *
     * @see DnsCacheManipulator#setDnsCache(java.util.Properties)
     * @see DnsCacheManipulator#loadDnsCacheConfig(java.lang.String)
     */
    public static void loadDnsCacheConfig() {
        loadDnsCacheConfig("dns-cache.properties");
    }

    /**
     * Load dns config from the specified properties file on classpath, then set dns cache.
     *
     * @param propertiesFileName specified properties file name on classpath.
     * @see DnsCacheManipulator#setDnsCache(java.util.Properties)
     */
    public static void loadDnsCacheConfig(String propertiesFileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName);
        if (inputStream == null) {
            inputStream = DnsCacheManipulator.class.getClassLoader().getResourceAsStream(propertiesFileName);
        }
        if (inputStream == null) {
            throw new DnsCacheManipulatorException("Fail to find " + propertiesFileName + " on classpath!");
        }

        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            setDnsCache(properties);
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to configDnsCacheByClassPathProperties, cause: " + e.toString(), e);
        }
    }

    /**
     * Get all dns cache entries.
     *
     * @return dns cache entries
     */
    public static List<DnsCacheEntry> getAllDnsCacheEntries() {
        try {
            return InetAddressCacheUtil.listInetAddressCache();
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to getAllDnsCacheEntries, cause: " + e.toString(), e);
        }
    }

    /**
     * Remove dns cache entry, cause lookup dns server for host after.
     *
     * @param host host
     * @see DnsCacheManipulator#clearDnsCacheEntry
     */
    public static void removeDnsCache(String host) {
        try {
            InetAddressCacheUtil.removeInetAddressCache(host);
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to removeDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Clear all dns cache entries, cause lookup dns server for all host after.
     */
    public static void clearDnsCacheEntry() {
        try {
            InetAddressCacheUtil.clearInetAddressCache();
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to clearDnsCacheEntry, cause: " + e.toString(), e);
        }
    }
}
