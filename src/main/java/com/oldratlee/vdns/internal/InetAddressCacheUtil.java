package com.oldratlee.vdns.internal;

import com.oldratlee.vdns.Host;

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

/**
 * @author ding.lid
 */
public class InetAddressCacheUtil {

    public static void setInetAddressCache(String host, String[] ips, long expiration)
            throws NoSuchMethodException, UnknownHostException,
            IllegalAccessException, InstantiationException, InvocationTargetException,
            ClassNotFoundException, NoSuchFieldException {
        Object entry = createCacheEntry(host, ips, expiration);
        synchronized (getAddressCacheFieldOfInetAddress()) {
            getCacheFiledOfInetAddress$CacheEntry().put(host, entry);
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


    @SuppressWarnings("unchecked")
    static Map<String, Object> getCacheFiledOfInetAddress$CacheEntry()
            throws NoSuchFieldException, IllegalAccessException {
        final Object inetAddressCache = getAddressCacheFieldOfInetAddress();
        Class clazz = inetAddressCache.getClass();

        final Field cacheMapField = clazz.getDeclaredField("cache");
        cacheMapField.setAccessible(true);
        return (Map<String, Object>) cacheMapField.get(inetAddressCache);
    }

    static volatile Object ADDRESS_CACHE = null;

    @SuppressWarnings("unchecked")
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
            address[i] = (byte) Integer.parseInt(ipParts[i]);
        }
        return address;
    }

    public static List<Host> listAllVirtualDns() throws NoSuchFieldException, IllegalAccessException {
        List<Host> list = new ArrayList<Host>();

        final Map<String, Object> cache;
        synchronized (getAddressCacheFieldOfInetAddress()) {
            cache = new HashMap<String, Object>(getCacheFiledOfInetAddress$CacheEntry());
        }

        for (Map.Entry<String, Object> entry : cache.entrySet()) {
            list.addAll(cacheEntry2Host(entry.getValue()));
        }
        return list;
    }

    static List<Host> cacheEntry2Host(Object entry) throws NoSuchFieldException, IllegalAccessException {
        Class<?> cacheEntryClazz = entry.getClass();

        Field expirationField = cacheEntryClazz.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        long expiration = (Long) expirationField.get(entry);

        Field addressesField = cacheEntryClazz.getDeclaredField("addresses");
        addressesField.setAccessible(true);
        InetAddress[] addresses = (InetAddress[]) addressesField.get(entry);

        List<Host> hosts = new ArrayList<Host>();
        for (InetAddress address : addresses) {
            Host host = new Host(address.getHostName(),
                    address.getHostAddress(), new Date(expiration));
            hosts.add(host);
        }
        return hosts;
    }
}
