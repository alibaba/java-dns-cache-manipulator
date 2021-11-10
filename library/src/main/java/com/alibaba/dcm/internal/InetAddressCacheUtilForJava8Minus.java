package com.alibaba.dcm.internal;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static com.alibaba.dcm.internal.InetAddressCacheUtilCommons.NEVER_EXPIRATION;
import static com.alibaba.dcm.internal.InetAddressCacheUtilCommons.toInetAddressArray;

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

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().put(host, entry);
            getCacheFiledOfNegativeCacheFiledOfInetAddress().remove(host);
        }
    }

    private static Object newCacheEntry(String host, String[] ips, long expiration)
            throws UnknownHostException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "java.net.InetAddress$CacheEntry";
        Class<?> clazz = Class.forName(className);

        // InetAddress.CacheEntry has only a constructor:
        // - for jdk 6, constructor signature is CacheEntry(Object address, long expiration)
        // - for jdk 7+, constructor signature is CacheEntry(InetAddress[] addresses, long expiration)
        // code in jdk 6:
        //   https://hg.openjdk.java.net/jdk6/jdk6/jdk/file/8deef18bb749/src/share/classes/java/net/InetAddress.java#l739
        // code in jdk 7:
        //   https://hg.openjdk.java.net/jdk7u/jdk7u/jdk/file/4dd5e486620d/src/share/classes/java/net/InetAddress.java#l742
        // code in jdk 8:
        //   https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/45e4e636b757/src/share/classes/java/net/InetAddress.java#l748
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(toInetAddressArray(host, ips), expiration);
    }

    public static void removeInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().remove(host);
            getCacheFiledOfNegativeCacheFiledOfInetAddress().remove(host);
        }
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}
     */
    @GuardedBy("getAddressCacheFieldOfInetAddress()")
    private static Map<String, Object> getCacheFiledOfAddressCacheFiledOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getCacheFiledOfInetAddress$Cache0(getAddressCacheFieldOfInetAddress());
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#negativeCache}
     */
    @GuardedBy("getAddressCacheFieldOfInetAddress()")
    private static Map<String, Object> getCacheFiledOfNegativeCacheFiledOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getCacheFiledOfInetAddress$Cache0(getNegativeCacheFieldOfInetAddress());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getCacheFiledOfInetAddress$Cache0(Object inetAddressCache)
            throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = inetAddressCache.getClass();

        final Field cacheMapField = clazz.getDeclaredField("cache");
        cacheMapField.setAccessible(true);
        return (Map<String, Object>) cacheMapField.get(inetAddressCache);
    }

    /**
     * @return {@link InetAddress#addressCache}
     */
    private static Object getAddressCacheFieldOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheFieldsOfInetAddress0()[0];
    }

    /**
     * @return {@link InetAddress#negativeCache}
     */
    private static Object getNegativeCacheFieldOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheFieldsOfInetAddress0()[1];
    }

    private static volatile Object[] ADDRESS_CACHE_AND_NEGATIVE_CACHE = null;

    /**
     * @return {@link InetAddress#addressCache} and {@link InetAddress#negativeCache}
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static Object[] getAddressCacheFieldsOfInetAddress0()
            throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE_AND_NEGATIVE_CACHE == null) {
            synchronized (InetAddressCacheUtilForJava8Minus.class) {
                if (ADDRESS_CACHE_AND_NEGATIVE_CACHE == null) {  // double check
                    final Field cacheField = InetAddress.class.getDeclaredField("addressCache");
                    cacheField.setAccessible(true);

                    final Field negativeCacheField = InetAddress.class.getDeclaredField("negativeCache");
                    negativeCacheField.setAccessible(true);

                    ADDRESS_CACHE_AND_NEGATIVE_CACHE = new Object[]{
                            cacheField.get(InetAddress.class),
                            negativeCacheField.get(InetAddress.class)
                    };
                }
            }
        }
        return ADDRESS_CACHE_AND_NEGATIVE_CACHE;
    }

    @Nullable
    public static DnsCacheEntry getInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        final Object cacheEntry;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cacheEntry = getCacheFiledOfAddressCacheFiledOfInetAddress().get(host);
        }

        if (null == cacheEntry) return null;

        final DnsCacheEntry dnsCacheEntry = inetAddress$CacheEntry2DnsCacheEntry(host, cacheEntry);
        if (isDnsCacheEntryExpired(dnsCacheEntry.getHost())) return null;

        return dnsCacheEntry;
    }

    private static boolean isDnsCacheEntryExpired(String host) {
        return null == host || "0.0.0.0".equals(host);
    }

    public static DnsCache listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException {
        final Map<String, Object> cache;
        final Map<String, Object> negativeCache;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cache = new HashMap<String, Object>(getCacheFiledOfAddressCacheFiledOfInetAddress());
            negativeCache = new HashMap<String, Object>(getCacheFiledOfNegativeCacheFiledOfInetAddress());
        }

        return new DnsCache(convert(cache), convert(negativeCache));
    }

    private static List<DnsCacheEntry> convert(Map<String, Object> cache) throws IllegalAccessException {
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


    private static volatile Field expirationFieldOfInetAddress$CacheEntry = null;
    private static volatile Field addressesFieldOfInetAddress$CacheEntry = null;

    private static DnsCacheEntry inetAddress$CacheEntry2DnsCacheEntry(String host, Object entry) throws IllegalAccessException {
        if (expirationFieldOfInetAddress$CacheEntry == null || addressesFieldOfInetAddress$CacheEntry == null) {
            synchronized (InetAddressCacheUtilForJava8Minus.class) {
                if (expirationFieldOfInetAddress$CacheEntry == null) { // double check
                    Class<?> cacheEntryClass = entry.getClass();
                    // InetAddress.CacheEntry has 2 filed:
                    // - for jdk 6, address and expiration
                    // - for jdk 7+, addresses(*renamed* from 6!) and expiration
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
        }

        long expiration = (Long) expirationFieldOfInetAddress$CacheEntry.get(entry);
        InetAddress[] addresses = (InetAddress[]) addressesFieldOfInetAddress$CacheEntry.get(entry);

        String[] ips = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            ips[i] = addresses[i].getHostAddress();
        }
        return new DnsCacheEntry(host, ips, expiration);
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().clear();
            getCacheFiledOfNegativeCacheFiledOfInetAddress().clear();
        }
    }

    private InetAddressCacheUtilForJava8Minus() {
    }
}
