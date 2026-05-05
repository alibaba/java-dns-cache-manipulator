package com.alibaba.dcm.internal;

import org.jetbrains.annotations.ApiStatus;
import sun.net.InetAddressCachePolicy;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.alibaba.dcm.internal.ReflectionUtils.*;

/**
 * Util class to manipulate dns cache.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
@ApiStatus.Internal
@SuppressWarnings("JavadocReference")
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

    @SuppressWarnings("DataFlowIssue")
    private static void setCachePolicy0(boolean isNegative, int seconds)
            throws NoSuchFieldException, IllegalAccessException {
        if (seconds < 0) {
            seconds = -1;
        }

        initFieldsOfInetAddressCachePolicy();

        synchronized (InetAddressCachePolicy.class) { // static synchronized method!
            if (isNegative) {
                negativeCachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
                if (negativeSetOfInetAddressCachePolicy != null) {
                    negativeSetOfInetAddressCachePolicy.setBoolean(null, true);
                }
            } else {
                cachePolicyFiledOfInetAddressCachePolicy.setInt(null, seconds);
                if (setFiledOfInetAddressCachePolicy != null) {
                    setFiledOfInetAddressCachePolicy.setBoolean(null, true);
                }
            }
        }
    }

    /**
     * {@link InetAddressCachePolicy.cachePolicy}
     */
    private static volatile Field cachePolicyFiledOfInetAddressCachePolicy;
    /**
     * {@link InetAddressCachePolicy.negativeCachePolicy}
     */
    private static volatile Field negativeCachePolicyFiledOfInetAddressCachePolicy;
    /**
     * {@link InetAddressCachePolicy.propertySet},
     * {@link InetAddressCachePolicy.set}, or absent (e.g. JDK 25+).
     */
    @Nullable
    private static volatile Field setFiledOfInetAddressCachePolicy;
    /**
     * {@link InetAddressCachePolicy.propertyNegativeSet},
     * {@link InetAddressCachePolicy.negativeSet}, or absent (e.g. JDK 25+).
     */
    @Nullable
    private static volatile Field negativeSetOfInetAddressCachePolicy;

    private static void initFieldsOfInetAddressCachePolicy() throws NoSuchFieldException {
        if (cachePolicyFiledOfInetAddressCachePolicy != null) return;

        final Class<?> clazz = InetAddressCachePolicy.class;
        synchronized (InetAddressCacheUtilCommons.class) {
            // double check
            if (cachePolicyFiledOfInetAddressCachePolicy != null) return;

            cachePolicyFiledOfInetAddressCachePolicy = getDeclaredFieldOrThrow(clazz, "cachePolicy");

            negativeCachePolicyFiledOfInetAddressCachePolicy = getDeclaredFieldOrThrow(clazz, "negativeCachePolicy");

            // JDK 25 removed these flags; propertySet / Set read the volatile int fields directly.
            setFiledOfInetAddressCachePolicy = getFallbackDeclaredFieldOrNull(clazz, "propertySet", "set");

            // JDK 25 removed these flags; get()/getNegative() read the volatile int fields directly.
            negativeSetOfInetAddressCachePolicy = getFallbackDeclaredFieldOrNull(
                    clazz, "propertyNegativeSet", "negativeSet");
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
            return isNew = hasDeclaredField(InetAddress.class, "expirySet");
        }
    }

    private InetAddressCacheUtilCommons() {}
}
