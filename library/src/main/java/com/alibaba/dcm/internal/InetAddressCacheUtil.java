package com.alibaba.dcm.internal;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import sun.net.InetAddressCachePolicy;

/**
 * Util class to manipulate dns cache {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}.
 * <p>
 * <b>Caution</b>: <br>
 * Manipulation on {@link InetAddress#addressCache} <strong>MUST</strong>
 * be guarded by {@link InetAddress#addressCache} to avoid multithreaded problem,
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
public class InetAddressCacheUtil {
    /**
     * Need convert host to lowercase, see {@link InetAddress#cacheAddresses(String, InetAddress[], boolean)}.
     */
    public static void setInetAddressCache(String host, String[] ips, long expiration)
            throws NoSuchMethodException, UnknownHostException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            ClassNotFoundException, NoSuchFieldException {
        host = host.toLowerCase();
        Object entry = newCacheEntry(host, ips, expiration);

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().put(host, entry);
            getCacheFiledOfNegativeCacheFiledOfInetAddress().remove(host);
        }
    }

    public static void removeInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().remove(host);
            getCacheFiledOfNegativeCacheFiledOfInetAddress().remove(host);
        }
    }

    static Object newCacheEntry(String host, String[] ips, long expiration)
            throws UnknownHostException, ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "java.net.InetAddress$CacheEntry";
        Class<?> clazz = Class.forName(className);

        // InetAddress.CacheEntry has only a constructor:
        // - for jdk 6, constructor signature is CacheEntry(Object address, long expiration)
        // - for jdk 7+, constructor signature is CacheEntry(InetAddress[] addresses, long expiration)
        // code in jdk 6:
        //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b27/java/net/InetAddress.java#InetAddress.CacheEntry
        // code in jdk 7:
        //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/net/InetAddress.java#InetAddress.CacheEntry
        // code in jdk 8:
        //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/net/InetAddress.java#InetAddress.CacheEntry
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(toInetAddressArray(host, ips), expiration);
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}
     */
    @GuardedBy("getAddressCacheFieldOfInetAddress()")
    static Map<String, Object> getCacheFiledOfAddressCacheFiledOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getCacheFiledOfInetAddress$Cache0(getAddressCacheFieldOfInetAddress());
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#negativeCache}
     */
    @GuardedBy("getAddressCacheFieldOfInetAddress()")
    static Map<String, Object> getCacheFiledOfNegativeCacheFiledOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getCacheFiledOfInetAddress$Cache0(getNegativeCacheFieldOfInetAddress());
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> getCacheFiledOfInetAddress$Cache0(Object inetAddressCache)
            throws NoSuchFieldException, IllegalAccessException {
        Class clazz = inetAddressCache.getClass();

        final Field cacheMapField = clazz.getDeclaredField("cache");
        cacheMapField.setAccessible(true);
        return (Map<String, Object>) cacheMapField.get(inetAddressCache);
    }

    /**
     * @return {@link InetAddress#addressCache}
     */
    static Object getAddressCacheFieldOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheFieldsOfInetAddress0()[0];
    }

    /**
     * @return {@link InetAddress#negativeCache}
     */
    static Object getNegativeCacheFieldOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return getAddressCacheFieldsOfInetAddress0()[1];
    }

    static volatile Object[] ADDRESS_CACHE_AND_NEGATIVE_CACHE = null;

    /**
     * @return {@link InetAddress#addressCache} and {@link InetAddress#negativeCache}
     */
    static Object[] getAddressCacheFieldsOfInetAddress0()
            throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE_AND_NEGATIVE_CACHE == null) {
            synchronized (InetAddressCacheUtil.class) {
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

    static InetAddress[] toInetAddressArray(String host, String[] ips) throws UnknownHostException {
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = InetAddress.getByAddress(host, IpParserUtil.ip2ByteArray(ips[i]));
        }

        return addresses;
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

    static boolean isDnsCacheEntryExpired(String host) {
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

        List<DnsCacheEntry> retCache = new ArrayList<DnsCacheEntry>();
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            final String host = entry.getKey();

            if (isDnsCacheEntryExpired(host)) { // exclude expired entries!
                continue;
            }
            retCache.add(inetAddress$CacheEntry2DnsCacheEntry(host, entry.getValue()));
        }
        List<DnsCacheEntry> retNegativeCache = new ArrayList<DnsCacheEntry>();
        for (Map.Entry<String, Object> entry : negativeCache.entrySet()) {
            final String host = entry.getKey();
            retNegativeCache.add(inetAddress$CacheEntry2DnsCacheEntry(host, entry.getValue()));
        }
        return new DnsCache(retCache, retNegativeCache);
    }


    static volatile Field expirationFieldOfInetAddress$CacheEntry = null;
    static volatile Field addressesFieldOfInetAddress$CacheEntry = null;

    static DnsCacheEntry inetAddress$CacheEntry2DnsCacheEntry(String host, Object entry)
            throws NoSuchFieldException, IllegalAccessException {
        if (expirationFieldOfInetAddress$CacheEntry == null || addressesFieldOfInetAddress$CacheEntry == null) {
            synchronized (InetAddressCacheUtil.class) {
                if (expirationFieldOfInetAddress$CacheEntry == null) { // double check
                    Class<?> cacheEntryClass = entry.getClass();
                    // InetAddress.CacheEntry has 2 filed:
                    // - for jdk 6, address and expiration
                    // - for jdk 7+, addresses(*renamed* from 6!) and expiration
                    // code in jdk 6:
                    //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b27/java/net/InetAddress.java#InetAddress.CacheEntry
                    // code in jdk 7:
                    //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/net/InetAddress.java#InetAddress.CacheEntry
                    // code in jdk 8:
                    //   http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/8-b132/java/net/InetAddress.java#InetAddress.CacheEntry
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
                                    " for class InetAddress.CacheEntry, report bug for dns-cache-manipulator lib!");
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
        return new DnsCacheEntry(host, ips, new Date(expiration));
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfAddressCacheFiledOfInetAddress().clear();
            getCacheFiledOfNegativeCacheFiledOfInetAddress().clear();
        }
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

    public static int getDnsCachePolicy()
            throws NoSuchFieldException, IllegalAccessException {
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

    public static int getDnsNegativeCachePolicy()
            throws NoSuchFieldException, IllegalAccessException {
        return InetAddressCachePolicy.getNegative();
    }


    static volatile Field setFiled$InetAddressCachePolicy = null;
    static volatile Field negativeSet$InetAddressCachePolicy = null;

    static void setCachePolicy0(boolean isNegative, int seconds)
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
                synchronized (InetAddressCacheUtil.class) {
                    if (negativeSet$InetAddressCachePolicy == null) {
                        try {
                            negativeSet$InetAddressCachePolicy = clazz.getDeclaredField("propertyNegativeSet");
                        } catch (NoSuchFieldException e) {
                            negativeSet$InetAddressCachePolicy = clazz.getDeclaredField("negativeSet");
                        }
                        negativeSet$InetAddressCachePolicy.setAccessible(true);
                    }
                }
            }
            setField = negativeSet$InetAddressCachePolicy;
        } else {
            if (setFiled$InetAddressCachePolicy == null) {
                synchronized (InetAddressCacheUtil.class) {
                    if (setFiled$InetAddressCachePolicy == null) {
                        try {
                            setFiled$InetAddressCachePolicy = clazz.getDeclaredField("propertySet");
                        } catch (NoSuchFieldException e) {
                            setFiled$InetAddressCachePolicy = clazz.getDeclaredField("set");
                        }
                        setFiled$InetAddressCachePolicy.setAccessible(true);
                    }
                }
            }
            setField = setFiled$InetAddressCachePolicy;
        }

        synchronized (InetAddressCachePolicy.class) { // static synchronized method! 
            cachePolicyFiled.set(null, seconds);
            setField.set(null, true);
        }
    }

    private InetAddressCacheUtil() {
    }
}
