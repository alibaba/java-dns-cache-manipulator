package com.alibaba.dcm.internal;

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
import java.util.regex.Pattern;

import javax.annotation.concurrent.GuardedBy;

/**
 * Util class to manipulate dns cache {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}.
 * <p/>
 * <b>Caution</b>: <br>
 * Manipulation on {@link InetAddress#addressCache} <strong>MUST</strong>
 * be guarded by {@link InetAddress#addressCache} to avoid multithreaded problem,
 * you can see the implementation of {@link InetAddress} to confirm this
 * (<b><i>See Also</i></b> lists key code of {@link InetAddress} related to this point).
 *
 * @author ding.lid
 * @see InetAddress
 * @see InetAddress#addressCache
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
        Object entry = createCacheEntry(host, ips, expiration);

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfInetAddress$CacheEntry().put(host, entry);
        }
    }

    public static void removeInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfInetAddress$CacheEntry().remove(host);
        }
    }

    static Object createCacheEntry(String host, String[] ips, long expiration)
            throws UnknownHostException, ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "java.net.InetAddress$CacheEntry";
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor(InetAddress[].class, long.class);
        constructor.setAccessible(true);
        return constructor.newInstance(toInetAddressArray(host, ips), expiration);
    }

    /**
     * @return {@link InetAddress.Cache#cache} in {@link InetAddress#addressCache}
     */
    @SuppressWarnings("unchecked")
    @GuardedBy("getAddressCacheFieldOfInetAddress()")
    static Map<String, Object> getCacheFiledOfInetAddress$CacheEntry()
            throws NoSuchFieldException, IllegalAccessException {
        final Object inetAddressCache = getAddressCacheFieldOfInetAddress();
        Class clazz = inetAddressCache.getClass();

        final Field cacheMapField = clazz.getDeclaredField("cache");
        cacheMapField.setAccessible(true);
        return (Map<String, Object>) cacheMapField.get(inetAddressCache);
    }

    static volatile Object ADDRESS_CACHE = null;

    /**
     * @return {@link InetAddress#addressCache}
     */
    static Object getAddressCacheFieldOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        if (ADDRESS_CACHE == null) {
            synchronized (InetAddressCacheUtil.class) {
                final Field cacheField = InetAddress.class.getDeclaredField("addressCache");
                cacheField.setAccessible(true);
                ADDRESS_CACHE = cacheField.get(InetAddress.class);
            }
        }
        return ADDRESS_CACHE;
    }

    static InetAddress[] toInetAddressArray(String host, String[] ips) throws UnknownHostException {
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = InetAddress.getByAddress(host, ip2ByteArray(ips[i]));
        }

        return addresses;
    }

    private static Pattern PATTERN_DOT = Pattern.compile("\\.");

    static byte[] ip2ByteArray(String ip) {
        final String[] ipParts = PATTERN_DOT.split(ip); // FIXME check ip validation

        byte[] address = new byte[ipParts.length];
        for (int i = 0; i < ipParts.length; i++) {
            final int part = Integer.parseInt(ipParts[i]);
            if (part < 0 || part > 255) {
                throw new IllegalStateException(ipParts[i] + "is not a byte!");
            }
            address[i] = (byte) part;
        }
        return address;
    }

    public static List<DnsCacheEntry> listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException {
        List<DnsCacheEntry> list = new ArrayList<DnsCacheEntry>();

        final Map<String, Object> cache;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cache = new HashMap<String, Object>(getCacheFiledOfInetAddress$CacheEntry());
        }

        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            list.addAll(inetAddress$CacheEntry2DnsCacheEntry(entry.getValue()));
        }
        return list;
    }

    static List<DnsCacheEntry> inetAddress$CacheEntry2DnsCacheEntry(Object entry)
            throws NoSuchFieldException, IllegalAccessException {
        Class<?> cacheEntryClass = entry.getClass();

        Field expirationField = cacheEntryClass.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        long expiration = (Long) expirationField.get(entry);

        Field addressesField = cacheEntryClass.getDeclaredField("addresses");
        addressesField.setAccessible(true);
        InetAddress[] addresses = (InetAddress[]) addressesField.get(entry);

        List<DnsCacheEntry> dnsCacheEntries = new ArrayList<DnsCacheEntry>();
        for (InetAddress address : addresses) {
            DnsCacheEntry dnsCacheEntry = new DnsCacheEntry(address.getHostName(),
                    address.getHostAddress(), new Date(expiration));
            dnsCacheEntries.add(dnsCacheEntry);
        }
        return dnsCacheEntries;
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfInetAddress$CacheEntry().clear();
        }
    }
}
