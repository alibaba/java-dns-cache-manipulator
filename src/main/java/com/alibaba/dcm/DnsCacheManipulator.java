package com.alibaba.dcm;

import com.alibaba.dcm.internal.InetAddressCacheUtil;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.Nullable;


/**
 * Setting dns (in fact dns cache).
 * <p/>
 * Throw {@link DnsCacheManipulatorException} if operation fail for all methods.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
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
     * @throws DnsCacheManipulatorException Operation fail
     * @see DnsCacheManipulator#setDnsCache(long, java.lang.String, java.lang.String...)
     */
    public static void setDnsCache(String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, NEVER_EXPIRATION);
        } catch (Exception e) {
            final String message = String.format("Fail to setDnsCache for host %s ip %s, cause: %s",
                    host, Arrays.toString(ips), e.toString());
            throw new DnsCacheManipulatorException(message, e);
        }
    }

    /**
     * Set a dns cache entry.
     *
     * @param expireMillis expire time in milliseconds.
     * @param host         host
     * @param ips          ips
     * @throws DnsCacheManipulatorException Operation fail
     */
    public static void setDnsCache(long expireMillis, String host, String... ips) {
        try {
            InetAddressCacheUtil.setInetAddressCache(host, ips, System.currentTimeMillis() + expireMillis);
        } catch (Exception e) {
            final String message = String.format("Fail to setDnsCache for host %s ip %s expireMillis %s, cause: %s",
                    host, Arrays.toString(ips), expireMillis, e.toString());
            throw new DnsCacheManipulatorException(message, e);
        }
    }

    private static Pattern COMMA_SEPARATOR = Pattern.compile("\\s*,\\s*");

    /**
     * Set dns cache entries by properties
     *
     * @param properties input properties. eg. {@code www.example.com=42.42.42.42}, or {@code www.example.com=42.42.42.42,43.43.43.43}
     * @throws DnsCacheManipulatorException Operation fail
     */
    public static void setDnsCache(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String host = (String) entry.getKey();
            String ipList = (String) entry.getValue();

            ipList = ipList.trim();
            if (ipList.isEmpty()) continue;

            final String[] ips = COMMA_SEPARATOR.split(ipList);
            setDnsCache(host, ips);
        }
    }

    /**
     * Load dns config from properties file {@code dns-cache.properties} on classpath, then set to dns cache.
     *
     * @throws DnsCacheManipulatorException Operation fail
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
     * @throws DnsCacheManipulatorException Operation fail
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
            final String message = String.format("Fail to loadDnsCacheConfig from %s, cause: %s",
                    propertiesFileName, e.toString());
            throw new DnsCacheManipulatorException(message, e);
        }
    }


    /**
     * Get dns cache.
     *
     * @return dns cache. return {@code null} if no entry for host or dns cache is expired.
     * @throws DnsCacheManipulatorException Operation fail
     */
    @Nullable
    public static DnsCacheEntry getDnsCache(String host) {
        try {
            return InetAddressCacheUtil.getInetAddressCache(host);
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to getDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Get all dns cache entries.
     *
     * @return dns cache entries
     * @throws DnsCacheManipulatorException Operation fail
     * @deprecated use {@link #listDnsCache} instead.
     */
    @Deprecated
    public static List<DnsCacheEntry> getAllDnsCache() {
        return listDnsCache();
    }

    /**
     * Get all dns cache entries.
     *
     * @return dns cache entries
     * @throws DnsCacheManipulatorException Operation fail
     * @see #getWholeDnsCache()
     * @since 1.2.0
     */
    public static List<DnsCacheEntry> listDnsCache() {
        try {
            return InetAddressCacheUtil.listInetAddressCache().getCache();
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to listDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Get whole dns cache info.
     *
     * @return dns cache entries
     * @throws DnsCacheManipulatorException Operation fail
     * @since 1.2.0
     */
    public static DnsCache getWholeDnsCache() {
        try {
            return InetAddressCacheUtil.listInetAddressCache();
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to getWholeDnsCache, cause: " + e.toString(), e);
        }
    }

    /**
     * Remove dns cache entry, cause lookup dns server for host after.
     *
     * @param host host
     * @throws DnsCacheManipulatorException Operation fail
     * @see DnsCacheManipulator#clearDnsCache
     */
    public static void removeDnsCache(String host) {
        try {
            InetAddressCacheUtil.removeInetAddressCache(host);
        } catch (Exception e) {
            final String message = String.format("Fail to removeDnsCache for host %s, cause: %s", host, e.toString());
            throw new DnsCacheManipulatorException(message, e);
        }
    }

    /**
     * Clear all dns cache entries, cause lookup dns server for all host after.
     *
     * @throws DnsCacheManipulatorException Operation fail
     */
    public static void clearDnsCache() {
        try {
            InetAddressCacheUtil.clearInetAddressCache();
        } catch (Exception e) {
            throw new DnsCacheManipulatorException("Fail to clearDnsCache, cause: " + e.toString(), e);
        }
    }

    private DnsCacheManipulator() {
    }
}
