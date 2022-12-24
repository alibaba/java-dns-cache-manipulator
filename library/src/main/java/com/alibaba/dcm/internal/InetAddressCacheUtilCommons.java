package com.alibaba.dcm.internal;

import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;
import org.jetbrains.annotations.ApiStatus;
import sun.net.InetAddressCachePolicy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Util class to manipulate dns cache.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
@ApiStatus.Internal
public final class InetAddressCacheUtilCommons {
    /**
     * We never really have "never".
     * <p>
     * {@code Long.MAX_VALUE(~9e18)} nanoseconds is ~292 years.
     * <p>
     * {@code Long.MAX_VALUE / 1e9 / 3600 / 24 / 365 ~= 292.47}.
     *
     * @see System#nanoTime()
     */
    public static final long NEVER_EXPIRATION = Long.MAX_VALUE;

    static InetAddress[] toInetAddressArray(String host, String[] ips) throws UnknownHostException {
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = InetAddress.getByAddress(host, IpParserUtil.ip2ByteArray(ips[i]));
        }
        return addresses;
    }

    static String[] getIpFromInetAddress(@Nullable InetAddress[] inetAddresses) {
        if (inetAddresses == null) return new String[0];

        final String[] ips = new String[inetAddresses.length];
        for (int i = 0; i < inetAddresses.length; i++) {
            ips[i] = inetAddresses[i].getHostAddress();
        }
        return ips;
    }

    /**
     * Set JVM DNS cache policy.
     *
     * @param cacheSeconds set default dns cache time. Special input case:
     *                     <ul>
     *                     <li> {@link InetAddressCachePolicy#FOREVER}({@code -1}) means never expired.(In effect, all negative value)</li>
     *                     <li> {@link InetAddressCachePolicy#NEVER}(@code 0) never cached.</li>
     *                     </ul>
     * @see InetAddressCachePolicy
     * @see InetAddressCachePolicy#cachePolicy
     * @see InetAddressCachePolicy#get()
     * @see InetAddressCachePolicy#FOREVER
     * @see InetAddressCachePolicy#NEVER
     * @see InetAddressCachePolicy#DEFAULT_POSITIVE
     */
    public static void setDnsCachePolicy(int cacheSeconds)
            throws NoSuchFieldException, IllegalAccessException {
        setCachePolicy0(false, cacheSeconds);
    }

    /**
     * Get JVM DNS cache policy.
     *
     * @see InetAddressCachePolicy#get()
     * @see InetAddressCachePolicy#FOREVER
     * @see InetAddressCachePolicy#NEVER
     * @see InetAddressCachePolicy#DEFAULT_POSITIVE
     */
    public static int getDnsCachePolicy() {
        return InetAddressCachePolicy.get();
    }

    /**
     * Set JVM DNS negative cache policy.
     *
     * @param negativeCacheSeconds set default dns cache time. Special input case:
     *                             <ul>
     *                             <li> {@link InetAddressCachePolicy#FOREVER}({@code -1}) means never expired.(In effect, all negative value)</li>
     *                             <li> {@link InetAddressCachePolicy#NEVER}(@code 0) never cached.</li>
     *                             </ul>
     * @see InetAddressCachePolicy
     * @see InetAddressCachePolicy#negativeCachePolicy
     * @see InetAddressCachePolicy#FOREVER
     * @see InetAddressCachePolicy#NEVER
     */
    public static void setDnsNegativeCachePolicy(int negativeCacheSeconds)
            throws NoSuchFieldException, IllegalAccessException {
        setCachePolicy0(true, negativeCacheSeconds);
    }

    /**
     * Get JVM DNS negative cache policy.
     *
     * @see InetAddressCachePolicy#getNegative()
     * @see InetAddressCachePolicy#FOREVER
     * @see InetAddressCachePolicy#NEVER
     */
    public static int getDnsNegativeCachePolicy() {
        return InetAddressCachePolicy.getNegative();
    }

    private static void setCachePolicy0(boolean isNegative, int seconds)
            throws NoSuchFieldException, IllegalAccessException {
        if (seconds < 0) {
            seconds = -1;
        }

        initFieldsOfInetAddressCachePolicy();

        synchronized (InetAddressCachePolicy.class) { // static synchronized method!
            if (isNegative) {
                negativeCachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
                negativeSetOfInetAddressCachePolicy.setBoolean(null, true);
            } else {
                cachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
                setFiledOfInetAddressCachePolicy.setBoolean(null, true);
            }
        }
    }

    /**
     * {@link InetAddressCachePolicy.cachePolicy}
     */
    private static volatile Field cachePolicyFiledOfInetAddressCachePolicy = null;
    /**
     * {@link InetAddressCachePolicy.negativeCachePolicy}
     */
    private static volatile Field negativeCachePolicyFiledOfInetAddressCachePolicy = null;
    /**
     * {@link InetAddressCachePolicy.propertySet}
     * or {@link InetAddressCachePolicy.set}
     */
    private static volatile Field setFiledOfInetAddressCachePolicy = null;
    /**
     * {@link InetAddressCachePolicy.propertyNegativeSet}
     * or {@link InetAddressCachePolicy.negativeSet}
     */
    private static volatile Field negativeSetOfInetAddressCachePolicy = null;

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void initFieldsOfInetAddressCachePolicy() throws NoSuchFieldException {
        if (negativeSetOfInetAddressCachePolicy != null) return;

        final Class<?> clazz = InetAddressCachePolicy.class;
        synchronized (InetAddressCacheUtilCommons.class) {
            // double check
            if (negativeSetOfInetAddressCachePolicy != null) return;

            Field f = clazz.getDeclaredField("cachePolicy");
            f.setAccessible(true);
            cachePolicyFiledOfInetAddressCachePolicy = f;

            f = clazz.getDeclaredField("negativeCachePolicy");
            f.setAccessible(true);
            negativeCachePolicyFiledOfInetAddressCachePolicy = f;

            try {
                f = clazz.getDeclaredField("propertySet");
            } catch (NoSuchFieldException e) {
                f = clazz.getDeclaredField("set");
            }
            f.setAccessible(true);
            setFiledOfInetAddressCachePolicy = f;

            try {
                f = clazz.getDeclaredField("propertyNegativeSet");
            } catch (NoSuchFieldException e) {
                f = clazz.getDeclaredField("negativeSet");
            }
            f.setAccessible(true);
            negativeSetOfInetAddressCachePolicy = f;
        }
    }

    private static volatile Boolean isNew;

    /**
     * Check the new or old implementation of {@link InetAddress}
     * by whether the field {@link InetAddress.expirySet} is existed or not.
     */
    public static boolean isNewInetAddressImpl() {
        if (isNew != null) return isNew;

        synchronized (InetAddressCacheUtilCommons.class) {
            // double check
            if (isNew != null) return isNew;

            try {
                InetAddress.class.getDeclaredField("expirySet");
                isNew = true;
            } catch (NoSuchFieldException e) {
                isNew = false;
            }

            return isNew;
        }
    }

    private InetAddressCacheUtilCommons() {
    }
}
