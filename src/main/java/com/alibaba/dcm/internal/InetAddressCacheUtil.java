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
                if (ADDRESS_CACHE == null) {  // double check
                    final Field cacheField = InetAddress.class.getDeclaredField("addressCache");
                    cacheField.setAccessible(true);
                    ADDRESS_CACHE = cacheField.get(InetAddress.class);
                }
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
        final String[] ipParts = PATTERN_DOT.split(ip);

        byte[] address = new byte[ipParts.length];
        for (int i = 0; i < ipParts.length; i++) {
            final int part = Integer.parseInt(ipParts[i]);
            if (part < 0 || part > 255) {
                throw new IllegalStateException(ipParts[i] + " is not a byte!");
            }
            address[i] = (byte) part;
        }
        return address;
    }

    public static DnsCacheEntry getInetAddressCache(String host)
            throws NoSuchFieldException, IllegalAccessException {
        host = host.toLowerCase();

        Object cacheEntry;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cacheEntry = getCacheFiledOfInetAddress$CacheEntry().get(host);
        }
        return inetAddress$CacheEntry2DnsCacheEntry(host, cacheEntry);
    }

    public static List<DnsCacheEntry> listInetAddressCache()
            throws NoSuchFieldException, IllegalAccessException {
        List<DnsCacheEntry> list = new ArrayList<DnsCacheEntry>();

        final Map<String, Object> cache;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cache = new HashMap<String, Object>(getCacheFiledOfInetAddress$CacheEntry());
        }

        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            final String host = entry.getKey();
            list.add(inetAddress$CacheEntry2DnsCacheEntry(host, entry.getValue()));
        }
        return list;
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
            getCacheFiledOfInetAddress$CacheEntry().clear();
        }
    }

    private InetAddressCacheUtil() {
    }
}
