package com.alibaba.dcm.internal;

import sun.net.InetAddressCachePolicy;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Util class to manipulate dns cache.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
public final class InetAddressCacheUtilCommons {
    /**
     * We never really have "never".
     * <p>
     * {@code Long.MAX_VALUE(~9e18)} nanoseconds is ~292 years.
     * <p>
     * {@code Long.MAX_VALUE / 1e9 / 3600 / 24 / 365 ~= 292.47}.
     */
    public static final long NEVER_EXPIRATION = Long.MAX_VALUE;

    static InetAddress[] toInetAddressArray(String host, String[] ips) throws UnknownHostException {
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = InetAddress.getByAddress(host, IpParserUtil.ip2ByteArray(ips[i]));
        }
        return addresses;
    }

    /**
     * Set JVM DNS cache policy
     *
     * @param cacheSeconds set default dns cache time. Special input case:
     *                     <ul>
     *                     <li> {@code -1} means never expired.(In effect, all negative value)</li>
     *                     <li> {@code 0} never cached.</li>
     *                     </ul>
     * @see InetAddressCachePolicy
     * @see InetAddressCachePolicy#cachePolicy
     */
    public static void setDnsCachePolicy(int cacheSeconds)
            throws NoSuchFieldException, IllegalAccessException {
        setCachePolicy0(false, cacheSeconds);
    }

    public static int getDnsCachePolicy() {
        return InetAddressCachePolicy.get();
    }

    /**
     * Set JVM DNS negative cache policy
     *
     * @param negativeCacheSeconds set default dns cache time. Special input case:
     *                             <ul>
     *                             <li> {@code -1} means never expired.(In effect, all negative value)</li>
     *                             <li> {@code 0} never cached.</li>
     *                             </ul>
     * @see InetAddressCachePolicy
     * @see InetAddressCachePolicy#negativeCachePolicy
     */
    public static void setDnsNegativeCachePolicy(int negativeCacheSeconds)
            throws NoSuchFieldException, IllegalAccessException {
        setCachePolicy0(true, negativeCacheSeconds);
    }

    public static int getDnsNegativeCachePolicy() {
        return InetAddressCachePolicy.getNegative();
    }

    private static volatile Field setFiled$InetAddressCachePolicy = null;
    private static volatile Field negativeSet$InetAddressCachePolicy = null;

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void setCachePolicy0(boolean isNegative, int seconds)
            throws NoSuchFieldException, IllegalAccessException {
        if (seconds < 0) {
            seconds = -1;
        }

        final Class<?> clazz = InetAddressCachePolicy.class;
        final Field cachePolicyFiled = clazz.getDeclaredField(
                isNegative ? "negativeCachePolicy" : "cachePolicy");
        cachePolicyFiled.setAccessible(true);

        final Field setField;
        if (isNegative) {
            if (negativeSet$InetAddressCachePolicy == null) {
                synchronized (InetAddressCacheUtilCommons.class) {
                    if (negativeSet$InetAddressCachePolicy == null) { // double check
                        Field f;
                        try {
                            f = clazz.getDeclaredField("propertyNegativeSet");
                        } catch (NoSuchFieldException e) {
                            f = clazz.getDeclaredField("negativeSet");
                        }
                        f.setAccessible(true);
                        negativeSet$InetAddressCachePolicy = f;
                    }
                }
            }
            setField = negativeSet$InetAddressCachePolicy;
        } else {
            if (setFiled$InetAddressCachePolicy == null) {
                synchronized (InetAddressCacheUtilCommons.class) {
                    if (setFiled$InetAddressCachePolicy == null) { // double check
                        Field f;
                        try {
                            f = clazz.getDeclaredField("propertySet");
                        } catch (NoSuchFieldException e) {
                            f = clazz.getDeclaredField("set");
                        }
                        f.setAccessible(true);
                        setFiled$InetAddressCachePolicy = f;
                    }
                }
            }
            setField = setFiled$InetAddressCachePolicy;
        }

        synchronized (InetAddressCachePolicy.class) { // static synchronized method!
            cachePolicyFiled.setInt(null, seconds);
            setField.setBoolean(null, true);
        }
    }

    private InetAddressCacheUtilCommons() {
    }
}
