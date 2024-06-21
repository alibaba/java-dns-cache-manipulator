package com.alibaba.dcm.internal;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;
import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static com.alibaba.dcm.internal.InetAddressCacheUtilCommons.*;
import static com.alibaba.dcm.internal.TimeUtil.convertNanoTimeToTimeMillis;
import static com.alibaba.dcm.internal.TimeUtil.getNanoTimeAfterMs;

/**
 * Util class to manipulate dns cache for new {@code JDK 9+}.
 * <p>
 * dns cache is {@link InetAddress#cache}.
 *
 * @author antfling (ding_zhengang at hithinksoft dot com)
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
@ApiStatus.Internal
@SuppressWarnings("JavadocReference")
public final class InetAddressCacheUtilForNew {
    /**
     * {@link InetAddress.CachedAddresses}
     * <p>
     * For jdk9+,
     * <ul>
     * <li>need not convert host to lowercase.<br>
     *     see {@link InetAddress.CachedAddresses#CachedAddresses}.
     * <li>{@code final long expiryTime; // time of expiry (in terms of System.nanoTime()) }<br>
     *     see {@link InetAddress.CachedAddresses.expiryTime}.
     * </ul>
     */
    public static void setInetAddressCache(String host, String[] ips, long expireMillis)
            throws UnknownHostException, IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassNotFoundException, NoSuchFieldException {
        long expiration = expireMillis == NEVER_EXPIRATION ? NEVER_EXPIRATION : getNanoTimeAfterMs(expireMillis);
        Object cachedAddresses = newCachedAddresses(host, ips, expiration);

        getCacheOfInetAddress().put(host, cachedAddresses);
        getExpirySetOfInetAddress().add(cachedAddresses);
    }

    /**
     * {@link InetAddress.CachedAddresses#CachedAddresses(String, InetAddress[], long)}
     */
    private static Object newCachedAddresses(String host, String[] ips, long expiration)
            throws ClassNotFoundException, UnknownHostException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        // InetAddress.CachedAddresses has only one constructor
        return getConstructorOfInetAddress$CachedAddresses().newInstance(host, toInetAddressArray(host, ips), expiration);
    }

    /**
     * {@link InetAddress.CachedAddresses#CachedAddresses}
     */
    private static volatile Constructor<?> constructorOfInetAddress$CachedAddresses = null;

    private static Constructor<?> getConstructorOfInetAddress$CachedAddresses() throws ClassNotFoundException {
        if (constructorOfInetAddress$CachedAddresses != null) return constructorOfInetAddress$CachedAddresses;

        synchronized (InetAddressCacheUtilCommons.class) {
            // double check
            if (constructorOfInetAddress$CachedAddresses != null) return constructorOfInetAddress$CachedAddresses;

            Class<?> clazz;

            try {
                clazz = Class.forName(inetAddress$CachedAddresses_ClassName);
            } catch (ClassNotFoundException e) {
                // jdk 21 support
                // due to https://github.com/openjdk/jdk/commit/8b127262a3dff9c4420945e902f6a688f8d05e2e
                clazz = Class.forName(inetAddress$CachedLookup_ClassName);
            }

            // InetAddress.CacheEntry has only one constructor:
            //
            // - for jdk 9-jdk12, constructor signature is CachedAddresses(String host, InetAddress[] inetAddresses, long expiryTime)
            // code in jdk 9:
            //   https://hg.openjdk.java.net/jdk9/jdk9/jdk/file/65464a307408/src/java.base/share/classes/java/net/InetAddress.java#l783
            // code in jdk 11:
            //   https://hg.openjdk.java.net/jdk/jdk11/file/1ddf9a99e4ad/src/java.base/share/classes/java/net/InetAddress.java#l787
            // code in jdk 21:
            //   https://github.com/openjdk/jdk/blob/jdk-21-ga/src/java.base/share/classes/java/net/InetAddress.java#L979
            final Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

            constructorOfInetAddress$CachedAddresses = constructor;
            return constructor;
        }
    }

    public static void removeInetAddressCache(String host) throws NoSuchFieldException, IllegalAccessException {
        removeHostFromExpirySetOfInetAddress(host);
        getCacheOfInetAddress().remove(host);
    }

    /**
     * @see #getExpirySetOfInetAddress()
     */
    private static void removeHostFromExpirySetOfInetAddress(String host)
            throws NoSuchFieldException, IllegalAccessException {
        for (Iterator<Object> iterator = getExpirySetOfInetAddress().iterator(); iterator.hasNext(); ) {
            Object cachedAddresses = iterator.next();
            if (getHostOfInetAddress$CacheAddress(cachedAddresses).equals(host)) {
                iterator.remove();
            }
        }
    }

    private static volatile Field hostFieldOfInetAddress$CacheAddress = null;

    /**
     * {@link InetAddress.CachedAddresses.host}
     */
    private static String getHostOfInetAddress$CacheAddress(Object cachedAddresses)
            throws NoSuchFieldException, IllegalAccessException {
        if (hostFieldOfInetAddress$CacheAddress == null) {
            synchronized (InetAddressCacheUtilForNew.class) {
                if (hostFieldOfInetAddress$CacheAddress == null) { // double check
                    final Field f = cachedAddresses.getClass().getDeclaredField("host");
                    f.setAccessible(true);
                    hostFieldOfInetAddress$CacheAddress = f;
                }
            }
        }
        return (String) hostFieldOfInetAddress$CacheAddress.get(cachedAddresses);
    }


    //////////////////////////////////////////////////////////////////////////////
    // getters of static cache related fields of InetAddress
    //////////////////////////////////////////////////////////////////////////////

    /**
     * return {@link InetAddress.cache} field.
     * <ul>
     * <li>type is {@code ConcurrentHashMap<String, Addresses>} type and thread-safe.
     * <li>contains values of type interface {@link InetAddress.Addresses}.
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private static ConcurrentMap<String, Object> getCacheOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return (ConcurrentMap<String, Object>) getCacheAndExpirySetOfInetAddress0()[0];
    }

    /**
     * @return {@link InetAddress.expirySet} field.
     * <p>
     * type is {@code ConcurrentSkipListSet<CachedAddresses>} and thread-safe.
     */
    @SuppressWarnings("unchecked")
    private static ConcurrentSkipListSet<Object> getExpirySetOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return (ConcurrentSkipListSet<Object>) getCacheAndExpirySetOfInetAddress0()[1];
    }

    private static volatile Object[] ADDRESS_CACHE_AND_EXPIRY_SET = null;

    /**
     * @return {@link InetAddress#cache} and {@link InetAddress#expirySet}
     */
    private static Object[] getCacheAndExpirySetOfInetAddress0()
            throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE_AND_EXPIRY_SET != null) return ADDRESS_CACHE_AND_EXPIRY_SET;

        synchronized (InetAddressCacheUtilForNew.class) {
            if (ADDRESS_CACHE_AND_EXPIRY_SET != null) return ADDRESS_CACHE_AND_EXPIRY_SET;

            final Field cacheField = InetAddress.class.getDeclaredField("cache");
            cacheField.setAccessible(true);

            final Field expirySetField = InetAddress.class.getDeclaredField("expirySet");
            expirySetField.setAccessible(true);

            ADDRESS_CACHE_AND_EXPIRY_SET = new Object[]{
                    cacheField.get(InetAddress.class),
                    expirySetField.get(InetAddress.class)
            };

            return ADDRESS_CACHE_AND_EXPIRY_SET;
        }
    }

    //////////////////////////////////////////////////////////////////////////////


    @Nullable
    public static DnsCacheEntry getInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final Object addresses = getCacheOfInetAddress().get(host);
        if (null == addresses) {
            return null;
        }

        return inetAddress$Addresses2DnsCacheEntry(host, addresses);
    }

    public static DnsCache listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final List<DnsCacheEntry> retCache = new ArrayList<>();
        final List<DnsCacheEntry> retNegativeCache = new ArrayList<>();

        final ConcurrentMap<String, Object> cache = getCacheOfInetAddress();
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            final String host = entry.getKey();

            DnsCacheEntry dnsCacheEntry = inetAddress$Addresses2DnsCacheEntry(host, entry.getValue());
            if (dnsCacheEntry.getIps().length == 0) {
                retNegativeCache.add(dnsCacheEntry);
            } else {
                retCache.add(dnsCacheEntry);
            }
        }

        return new DnsCache(retCache, retNegativeCache);
    }

    /**
     * convert {@link InetAddress.Addresses} to {@link DnsCacheEntry}
     */
    private static DnsCacheEntry inetAddress$Addresses2DnsCacheEntry(String host, Object addresses)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final String addressesClassName = addresses.getClass().getName();

        initFieldsOfAddresses();

        final InetAddress[] inetAddresses;
        final long expiration;
        if (addressesClassName.equals(inetAddress$CachedAddresses_ClassName)
                // jdk 21 support
                || addressesClassName.equals(inetAddress$CachedLookup_ClassName)) {

            inetAddresses = (InetAddress[]) inetAddressesFieldOfInetAddress$CacheAddress.get(addresses);

            long expiryTimeNanos = expiryTimeFieldOfInetAddress$CacheAddress.getLong(addresses);
            expiration = convertNanoTimeToTimeMillis(expiryTimeNanos);
        } else if (addressesClassName.equals(inetAddress$NameServiceAddresses_ClassName)) {
            throw new IllegalStateException("child class " + addressesClassName +
                    " for class InetAddress.Addresses should never happens, report issue for dns-cache-manipulator lib!");
        } else {
            throw new IllegalStateException("JDK add new child class " + addressesClassName +
                    " for class InetAddress.Addresses, report issue for dns-cache-manipulator lib!");
        }

        final String[] ips = getIpFromInetAddress(inetAddresses);

        return new DnsCacheEntry(host, ips, expiration);
    }

    private static final String inetAddress$CachedAddresses_ClassName = "java.net.InetAddress$CachedAddresses";
    private static final String inetAddress$CachedLookup_ClassName = "java.net.InetAddress$CachedLookup";
    private static final String inetAddress$NameServiceAddresses_ClassName = "java.net.InetAddress$NameServiceAddresses";

    // Fields of InetAddress$CachedAddresses
    /**
     * {@link InetAddress.CachedAddresses.inetAddresses}
     */
    private static volatile Field inetAddressesFieldOfInetAddress$CacheAddress = null;
    /**
     * {@link InetAddress.CachedAddresses.expiryTime}
     */
    private static volatile Field expiryTimeFieldOfInetAddress$CacheAddress = null;

    private static void initFieldsOfAddresses() throws ClassNotFoundException, NoSuchFieldException {
        if (inetAddressesFieldOfInetAddress$CacheAddress != null) return;

        synchronized (InetAddressCacheUtilForNew.class) {
            if (inetAddressesFieldOfInetAddress$CacheAddress != null) return;

            ///////////////////////////////////////////////
            // Fields of InetAddress$CachedAddresses
            ///////////////////////////////////////////////
            Class<?> cachedAddresses_Class;

            try {
                cachedAddresses_Class = Class.forName(inetAddress$CachedAddresses_ClassName);
            } catch (ClassNotFoundException e) {
                // jdk 21 support
                cachedAddresses_Class = Class.forName(inetAddress$CachedLookup_ClassName);
            }

            final Field inetAddressesFiled = cachedAddresses_Class.getDeclaredField("inetAddresses");
            inetAddressesFiled.setAccessible(true);
            inetAddressesFieldOfInetAddress$CacheAddress = inetAddressesFiled;

            final Field expiryTimeFiled = cachedAddresses_Class.getDeclaredField("expiryTime");
            expiryTimeFiled.setAccessible(true);
            expiryTimeFieldOfInetAddress$CacheAddress = expiryTimeFiled;
        }
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        getExpirySetOfInetAddress().clear();
        getCacheOfInetAddress().clear();
    }

    private InetAddressCacheUtilForNew() {
    }
}
