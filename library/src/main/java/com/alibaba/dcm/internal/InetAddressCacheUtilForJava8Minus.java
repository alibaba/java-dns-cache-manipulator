package com.alibaba.dcm.internal;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;
import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.dcm.internal.InetAddressCacheUtilCommons.*;

/**
 * Util class to manipulate dns cache for {@code JDK 8-}.
 * <p>
 * dns cache is {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}.
 * <p>
 * <b>Caution</b>: <br>
 * Manipulation on {@link InetAddress#addressCache} <strong>MUST</strong>
 * be guarded by {@link InetAddress#addressCache} to avoid multithreading problem,
 * you can see the implementation of {@link InetAddress} to confirm this
 * (<b><i>See Also</i></b> lists key code of {@link InetAddress} related to this point).
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see InetAddress
 * @see InetAddress#addressCache
 * @see InetAddress.CacheEntry
 * @see InetAddress#cacheInitIfNeeded()
 * @see InetAddress#cacheAddresses(String, InetAddress[], boolean)
 */
@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
public final class InetAddressCacheUtilForJava8Minus {
    /**
     * Need convert host to lowercase, see {@link InetAddress#cacheAddresses(String, InetAddress[], boolean)}.
     */
    public static void setInetAddressCache(String host, String[] ips, long expireMillis) throws UnknownHostException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            ClassNotFoundException, NoSuchFieldException {
        host = host.toLowerCase();
        long expiration = expireMillis == NEVER_EXPIRATION ? NEVER_EXPIRATION : System.currentTimeMillis() + expireMillis;
        Object entry = newCacheEntry(host, ips, expiration);

        synchronized (getAddressCacheOfInetAddress()) {
            getCache().put(host, entry);
            getNegativeCache().remove(host);
        }
    }

    private static Object newCacheEntry(String host, String[] ips, long expiration)
            throws UnknownHostException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // InetAddress.CacheEntry has only one constructor
        return getConstructorOfInetAddress$CacheEntry().newInstance(toInetAddressArray(host, ips), expiration);
    }

    /**
     * {@link InetAddress.CacheEntry#CacheEntry}
     */
    private static volatile Constructor<?> constructorOfInetAddress$CacheEntry = null;

    private static Constructor<?> getConstructorOfInetAddress$CacheEntry() throws ClassNotFoundException {
        if (constructorOfInetAddress$CacheEntry != null) return constructorOfInetAddress$CacheEntry;

        synchronized (InetAddressCacheUtilCommons.class) {
            // double check
            if (constructorOfInetAddress$CacheEntry != null) return constructorOfInetAddress$CacheEntry;

            final String className = "java.net.InetAddress$CacheEntry";
            final Class<?> clazz = Class.forName(className);

            // InetAddress.CacheEntry has only one constructor:
            // - for jdk 6, constructor signature is CacheEntry(Object address, long expiration)
            // - for jdk 7/8, constructor signature is CacheEntry(InetAddress[] addresses, long expiration)
            //
            // code in jdk 6:
            //   https://hg.openjdk.java.net/jdk6/jdk6/jdk/file/8deef18bb749/src/share/classes/java/net/InetAddress.java#l739
            // code in jdk 7:
            //   https://hg.openjdk.java.net/jdk7u/jdk7u/jdk/file/4dd5e486620d/src/share/classes/java/net/InetAddress.java#l742
            // code in jdk 8:
            //   https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/45e4e636b757/src/share/classes/java/net/InetAddress.java#l748
            final Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

            constructorOfInetAddress$CacheEntry = constructor;
            return constructor;
        }
    }

    public static void removeInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        host = host.toLowerCase();

        synchronized (getAddressCacheOfInetAddress()) {
            getCache().remove(host);
            getNegativeCache().remove(host);
        }
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}
     */
    @GuardedBy("getAddressCacheOfInetAddress()")
    private static Map<String, Object> getCache()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getCacheOfInetAddress$Cache0(getAddressCacheOfInetAddress());
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#negativeCache}
     */
    @GuardedBy("getAddressCacheOfInetAddress()")
    private static Map<String, Object> getNegativeCache()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getCacheOfInetAddress$Cache0(getNegativeCacheOfInetAddress());
    }


    /**
     * {@link InetAddress.Cache.cache}
     */
    private static volatile Field cacheMapFieldOfInetAddress$Cache = null;

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getCacheOfInetAddress$Cache0(Object inetAddressCache)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        if (cacheMapFieldOfInetAddress$Cache == null) {
            synchronized (InetAddressCacheUtilForJava8Minus.class) {
                if (cacheMapFieldOfInetAddress$Cache == null) { // double check
                    final Class<?> clazz = Class.forName("java.net.InetAddress$Cache");
                    final Field f = clazz.getDeclaredField("cache");
                    f.setAccessible(true);
                    cacheMapFieldOfInetAddress$Cache = f;
                }
            }
        }

        return (Map<String, Object>) cacheMapFieldOfInetAddress$Cache.get(inetAddressCache);
    }

    /**
     * @return {@link InetAddress#addressCache}
     */
    private static Object getAddressCacheOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheAndNegativeCacheOfInetAddress0()[0];
    }

    /**
     * @return {@link InetAddress#negativeCache}
     */
    private static Object getNegativeCacheOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheAndNegativeCacheOfInetAddress0()[1];
    }

    private static volatile Object[] ADDRESS_CACHE_AND_NEGATIVE_CACHE = null;

    /**
     * @return {@link InetAddress#addressCache} and {@link InetAddress#negativeCache}
     */
    private static Object[] getAddressCacheAndNegativeCacheOfInetAddress0()
            throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE_AND_NEGATIVE_CACHE != null) return ADDRESS_CACHE_AND_NEGATIVE_CACHE;

        synchronized (InetAddressCacheUtilForJava8Minus.class) {
            // double check
            if (ADDRESS_CACHE_AND_NEGATIVE_CACHE != null) return ADDRESS_CACHE_AND_NEGATIVE_CACHE;

            final Field cacheField = InetAddress.class.getDeclaredField("addressCache");
            cacheField.setAccessible(true);

            final Field negativeCacheField = InetAddress.class.getDeclaredField("negativeCache");
            negativeCacheField.setAccessible(true);

            ADDRESS_CACHE_AND_NEGATIVE_CACHE = new Object[]{
                    cacheField.get(InetAddress.class),
                    negativeCacheField.get(InetAddress.class)
            };
            return ADDRESS_CACHE_AND_NEGATIVE_CACHE;
        }
    }

    @Nullable
    public static DnsCacheEntry getInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        host = host.toLowerCase();

        final Object cacheEntry;
        synchronized (getAddressCacheOfInetAddress()) {
            cacheEntry = getCache().get(host);
        }

        if (null == cacheEntry) return null;

        final DnsCacheEntry dnsCacheEntry = inetAddress$CacheEntry2DnsCacheEntry(host, cacheEntry);
        if (isDnsCacheEntryExpired(dnsCacheEntry.getHost())) return null;

        return dnsCacheEntry;
    }

    private static boolean isDnsCacheEntryExpired(@Nullable String host) {
        return null == host || "0.0.0.0".equals(host);
    }

    public static DnsCache listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        final Map<String, Object> cache;
        final Map<String, Object> negativeCache;
        synchronized (getAddressCacheOfInetAddress()) {
            cache = new HashMap<String, Object>(getCache());
            negativeCache = new HashMap<String, Object>(getNegativeCache());
        }

        return new DnsCache(convert(cache), convert(negativeCache));
    }

    private static List<DnsCacheEntry> convert(Map<String, Object> cache) throws IllegalAccessException, ClassNotFoundException {
        final List<DnsCacheEntry> ret = new ArrayList<DnsCacheEntry>();
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            final String host = entry.getKey();
            if (isDnsCacheEntryExpired(host)) { // exclude expired entries!
                continue;
            }

            ret.add(inetAddress$CacheEntry2DnsCacheEntry(host, entry.getValue()));
        }
        return ret;
    }

    private static DnsCacheEntry inetAddress$CacheEntry2DnsCacheEntry(String host, Object entry) throws IllegalAccessException, ClassNotFoundException {
        initFieldsOfInetAddress$CacheEntry();

        final long expiration = expirationFieldOfInetAddress$CacheEntry.getLong(entry);

        final InetAddress[] addresses = (InetAddress[]) addressesFieldOfInetAddress$CacheEntry.get(entry);
        final String[] ips = getIpFromInetAddress(addresses);

        return new DnsCacheEntry(host, ips, expiration);
    }

    /**
     * {@link InetAddress.CacheEntry.expiration}
     */
    private static volatile Field expirationFieldOfInetAddress$CacheEntry = null;
    /**
     * {@link InetAddress.CacheEntry.expiration}
     */
    private static volatile Field addressesFieldOfInetAddress$CacheEntry = null;

    private static void initFieldsOfInetAddress$CacheEntry() throws ClassNotFoundException {
        if (expirationFieldOfInetAddress$CacheEntry != null && addressesFieldOfInetAddress$CacheEntry != null) return;

        final Class<?> cacheEntryClass = Class.forName("java.net.InetAddress$CacheEntry");
        synchronized (InetAddressCacheUtilForJava8Minus.class) {
            // double check
            if (expirationFieldOfInetAddress$CacheEntry != null && addressesFieldOfInetAddress$CacheEntry != null) return;

            // InetAddress.CacheEntry has 2 filed:
            // - for jdk 6, address and expiration
            // - for jdk 7+, addresses(*renamed*!) and expiration
            // code in jdk 6:
            //   https://hg.openjdk.java.net/jdk6/jdk6/jdk/file/8deef18bb749/src/share/classes/java/net/InetAddress.java#l739
            // code in jdk 7:
            //   https://hg.openjdk.java.net/jdk7u/jdk7u/jdk/file/4dd5e486620d/src/share/classes/java/net/InetAddress.java#l742
            // code in jdk 8:
            //   https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/45e4e636b757/src/share/classes/java/net/InetAddress.java#l748
            final Field[] fields = cacheEntryClass.getDeclaredFields();
            for (Field field : fields) {
                final String name = field.getName();
                if (name.equals("expiration")) {
                    field.setAccessible(true);
                    expirationFieldOfInetAddress$CacheEntry = field;
                } else if (name.startsWith("address")) { // use startWith so works for jdk 6 and jdk 7+
                    field.setAccessible(true);
                    addressesFieldOfInetAddress$CacheEntry = field;
                } else {
                    throw new IllegalStateException("JDK add new Field " + name +
                            " for class InetAddress.CacheEntry, report issue for dns-cache-manipulator lib!");
                }
            }
        }
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        synchronized (getAddressCacheOfInetAddress()) {
            getCache().clear();
            getNegativeCache().clear();
        }
    }

    private InetAddressCacheUtilForJava8Minus() {
    }
}
