package com.alibaba.dcm.internal;

import com.alibaba.dcm.DnsCache;
import com.alibaba.dcm.DnsCacheEntry;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.alibaba.dcm.internal.InetAddressCacheUtil.isDnsCacheEntryExpired;
import static com.alibaba.dcm.internal.InetAddressCacheUtil.toInetAddressArray;

/**
 * @author dzg
 * @date 2019/4/4
 */
public class InetAddressJdk9PlusCacheUtil {
    /**
     * Need convert host to lowercase, see {@link InetAddress#cacheAddresses(String, InetAddress[], boolean)}.
     */
    public static void setInetAddressCache(String host, String[] ips, long expiration)
            throws NoSuchMethodException, UnknownHostException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            ClassNotFoundException, NoSuchFieldException {
        host = host.toLowerCase();
        Object entry = newCachedAddresses(host, ips, expiration);

        synchronized (getCacheAndExpirySetFieldOfInetAddress0()) {
            getCacheFieldOfInetAddress().put(host, entry);
            removeExpirySetFieldOfInetAddressByHost(host);
        }
    }

    public static void removeInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        host = host.toLowerCase();

        synchronized (getCacheAndExpirySetFieldOfInetAddress0()) {
            getCacheFieldOfInetAddress().remove(host);
            removeExpirySetFieldOfInetAddressByHost(host);
        }
    }


    /**
     * @param host remove this host from expirySet
     */
    static void removeExpirySetFieldOfInetAddressByHost(String host) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        NavigableSet<Object> expirySetFieldOfInetAddress = getExpirySetFieldOfInetAddress();
        Iterator<Object> iterator = expirySetFieldOfInetAddress.iterator();
        Field hostField = getHostFieldOfInetAddress$CacheAddress();
        while (iterator.hasNext()) {
            Object expiry = iterator.next();
            if (hostField.get(expiry).equals(host)) {
                iterator.remove();
            }
        }
    }

    static Field getHostFieldOfInetAddress$CacheAddress() throws ClassNotFoundException, NoSuchFieldException {
        if (hostFieldOfInetAddress$CacheAddress == null) {
            synchronized (InetAddressJdk9PlusCacheUtil.class) {
                if (hostFieldOfInetAddress$CacheAddress == null) {
                    String className = "java.net.InetAddress$CachedAddresses";
                    Class<?> clazz = Class.forName(className);
                    hostFieldOfInetAddress$CacheAddress = clazz.getDeclaredField("host");
                    hostFieldOfInetAddress$CacheAddress.setAccessible(true);
                }
            }
        }
        return hostFieldOfInetAddress$CacheAddress;
    }

    static Object newCachedAddresses(String host, String[] ips, long expiration) throws ClassNotFoundException, UnknownHostException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "java.net.InetAddress$CachedAddresses";
        Class<?> clazz = Class.forName(className);
        // InetAddress.CachedAddresses has only a constructor:
        // - for jdk 9-jdk12, constructor signature is  CachedAddresses(String host, InetAddress[] inetAddresses, long expiryTime)
        // code in jdk 9-jdk12:
        //  http://hg.openjdk.java.net/jdk9/jdk9/jdk/file/65464a307408/src/java.base/share/classes/java/net/InetAddress.java#783
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(host, toInetAddressArray(host, ips), expiration);
    }

    /**
     * @return {@link InetAddress#cache}
     */
    @SuppressWarnings("unchecked")
    @GuardedBy("getCacheAndExpirySetFieldOfInetAddress0()")
    static Map<String, Object> getCacheFieldOfInetAddress() throws NoSuchFieldException, IllegalAccessException {
        return (Map<String, Object>) getCacheAndExpirySetFieldOfInetAddress0()[0];
    }

    /**
     * @return {@link InetAddress#expirySet}
     */
    @SuppressWarnings("unchecked")
    @GuardedBy("getCacheAndExpirySetFieldOfInetAddress0()")
    static NavigableSet<Object> getExpirySetFieldOfInetAddress() throws NoSuchFieldException, IllegalAccessException {
        return (NavigableSet<Object>) getCacheAndExpirySetFieldOfInetAddress0()[1];
    }

    static volatile Object[] ADDRESS_CACHE_AND_EXPIRY_SET = null;

    /**
     * @return {@link InetAddress#cache} and {@link InetAddress#expirySet}
     */
    static Object[] getCacheAndExpirySetFieldOfInetAddress0() throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE_AND_EXPIRY_SET == null) {
            synchronized (InetAddressJdk9PlusCacheUtil.class) {
                if (ADDRESS_CACHE_AND_EXPIRY_SET == null) {
                    final Field cacheField = InetAddress.class.getDeclaredField("cache");
                    cacheField.setAccessible(true);

                    final Field expirySetField = InetAddress.class.getDeclaredField("expirySet");
                    expirySetField.setAccessible(true);

                    ADDRESS_CACHE_AND_EXPIRY_SET = new Object[]{
                            cacheField.get(InetAddress.class),
                            expirySetField.get(InetAddress.class)
                    };
                }
            }
        }
        return ADDRESS_CACHE_AND_EXPIRY_SET;
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        synchronized (getCacheAndExpirySetFieldOfInetAddress0()) {
            getCacheFieldOfInetAddress().clear();
            getExpirySetFieldOfInetAddress().clear();
        }
    }

    @Nullable
    public static DnsCacheEntry getInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        final Object cacheAddress;
        synchronized (getCacheAndExpirySetFieldOfInetAddress0()) {
            cacheAddress = getCacheFieldOfInetAddress().get(host);
        }

        if (null == cacheAddress) {
            return null;
        }

        final DnsCacheEntry dnsCacheEntry = inetAddress$CacheAddress2DnsCacheEntry(host, cacheAddress);
        if (isDnsCacheEntryExpired(dnsCacheEntry.getHost())) {
            return null;
        }
        return dnsCacheEntry;
    }

    public static DnsCache listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {

        final Map<String, Object> cache;
        final NavigableSet<Object> negativeCache;
        synchronized (getCacheAndExpirySetFieldOfInetAddress0()) {
            cache = getCacheFieldOfInetAddress();
            negativeCache = getExpirySetFieldOfInetAddress();
        }

        List<DnsCacheEntry> retCache = new ArrayList<DnsCacheEntry>();
        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            final String host = entry.getKey();

            if (isDnsCacheEntryExpired(host)) { // exclude expired entries!
                continue;
            }
            retCache.add(inetAddress$CacheAddress2DnsCacheEntry(host, entry.getValue()));
        }
        List<DnsCacheEntry> retNegativeCache = new ArrayList<DnsCacheEntry>();
        for (Object entry : negativeCache) {
            final String host = (String) getHostFieldOfInetAddress$CacheAddress().get(entry);
            retNegativeCache.add(inetAddress$CacheAddress2DnsCacheEntry(host, entry));
        }
        return new DnsCache(retCache, retNegativeCache);
    }

    static volatile Field hostFieldOfInetAddress$CacheAddress = null;
    static volatile Field inetAddressesFieldOfInetAddress$CacheAddress = null;
    static volatile Field expiryTimeFieldOfInetAddress$CacheAddress = null;
    static volatile Field reqAddrFieldOfInetAddress$CacheAddress = null;

    private static final String NAME_SERVICE_ADDRESS = "NameServiceAddresses";
    private static final String CACHED_ADDRESS = "CachedAddresses";
    private static final Long NEVER_EXPIRY = Long.MAX_VALUE;

    static DnsCacheEntry inetAddress$CacheAddress2DnsCacheEntry(String host, Object cacheAddress) throws NoSuchFieldException, IllegalAccessException {
        Class<?> addressClass = cacheAddress.getClass();
        if (inetAddressesFieldOfInetAddress$CacheAddress == null || expiryTimeFieldOfInetAddress$CacheAddress == null || reqAddrFieldOfInetAddress$CacheAddress == null) {
            synchronized (InetAddressJdk9PlusCacheUtil.class) {
                if (addressClass.getName().contains(NAME_SERVICE_ADDRESS)) {
                    if (reqAddrFieldOfInetAddress$CacheAddress == null) {
                        Field inetAddressesFiled = addressClass.getDeclaredField("reqAddr");
                        inetAddressesFiled.setAccessible(true);
                        reqAddrFieldOfInetAddress$CacheAddress = inetAddressesFiled;
                    }
                } else if (addressClass.getName().contains(CACHED_ADDRESS)) {
                    if (inetAddressesFieldOfInetAddress$CacheAddress == null) {
                        Field inetAddressesFiled = addressClass.getDeclaredField("inetAddresses");
                        inetAddressesFiled.setAccessible(true);
                        inetAddressesFieldOfInetAddress$CacheAddress = inetAddressesFiled;
                    }
                    if (expiryTimeFieldOfInetAddress$CacheAddress == null) {
                        Field inetAddressesFiled = addressClass.getDeclaredField("expiryTime");
                        inetAddressesFiled.setAccessible(true);
                        expiryTimeFieldOfInetAddress$CacheAddress = inetAddressesFiled;
                    }
                } else {
                    throw new IllegalStateException("JDK add new child class " + addressClass.getName() +
                            " for class InetAddress.Addresses, report bug for dns-cache-manipulator lib!");
                }

            }

        }
        InetAddress[] addresses;
        long expiration;
        if (addressClass.getName().contains(CACHED_ADDRESS)) {
            expiration = (Long) expiryTimeFieldOfInetAddress$CacheAddress.get(cacheAddress);
            addresses = (InetAddress[]) inetAddressesFieldOfInetAddress$CacheAddress.get(cacheAddress);
        } else {
            addresses = (InetAddress[]) reqAddrFieldOfInetAddress$CacheAddress.get(cacheAddress);
            expiration = NEVER_EXPIRY;
        }
        String[] ips = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            ips[i] = addresses[i].getHostAddress();
        }
        return new DnsCacheEntry(host, ips, new Date(expiration));
    }

    private InetAddressJdk9PlusCacheUtil() {
    }
}
