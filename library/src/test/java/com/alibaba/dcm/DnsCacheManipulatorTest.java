package com.alibaba.dcm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheManipulatorTest {
    private static final String DOMAIN1 = "www.hello1.com";
    private static final String IP1 = "42.42.41.41";
    private static final String DOMAIN2 = "www.hello2.com";
    private static final String IP2 = "42.42.41.42";
    private static final String IP3 = "42.42.43.43";
    private static final String DOMAIN_CUSTOMIZED = "www.customized.com";
    private static final String IP_CUSTOMIZED = "42.42.42.42";

    private static final String DOMAIN_NOT_EXISTED = "www.domain-not-existed-7352jt-12559-AZ-7524087.com";

    @BeforeClass
    public static void beforeClass() {
        // System Properties
        // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.printf("Env info:\njava home: %s\njdk version: %s\n",
                System.getProperty("java.home"),
                System.getProperty("java.version"));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Before
    @SuppressWarnings("deprecation")
    public void before() {
        DnsCacheManipulator.clearDnsCache();
        assertTrue(DnsCacheManipulator.getAllDnsCache().isEmpty());
        assertTrue(DnsCacheManipulator.getWholeDnsCache().getNegativeCache().isEmpty());
    }

    @Test
    public void test_loadDnsCacheConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig();
        final String ip = InetAddress.getByName(DOMAIN1).getHostAddress();
        assertEquals(IP1, ip);
    }

    @Test
    public void test_loadDnsCacheConfig_from_D_Option() throws Exception {
        final String key = "dcm.config.filename";
        System.setProperty(key, "customized-dns-cache.properties");
        DnsCacheManipulator.loadDnsCacheConfig();
        final String ip = InetAddress.getByName(DOMAIN_CUSTOMIZED).getHostAddress();
        assertEquals(IP_CUSTOMIZED, ip);
        System.clearProperty(key);
    }

    @Test
    public void test_loadDnsCacheConfig_fromMyConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
        final String ip = InetAddress.getByName(DOMAIN2).getHostAddress();
        assertEquals(IP2, ip);
    }

    @Test
    public void test_setMultiIp() throws Exception {
        DnsCacheManipulator.setDnsCache("multi.ip.com", "1.1.1.1", "2.2.2.2");
        String ip = InetAddress.getByName("multi.ip.com").getHostAddress();
        assertEquals("1.1.1.1", ip);

        InetAddress[] all = InetAddress.getAllByName("multi.ip.com");
        assertEquals(2, all.length);
        String[] ips = {all[0].getHostAddress(), all[1].getHostAddress()};
        assertArrayEquals(new String[]{"1.1.1.1", "2.2.2.2"}, ips);
    }

    @Test
    public void test_configNotFound() {
        try {
            DnsCacheManipulator.loadDnsCacheConfig("not-existed.properties");
            fail();
        } catch (DnsCacheManipulatorException expected) {
            assertEquals("Fail to find not-existed.properties on classpath!", expected.getMessage());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_setDnsCache_getAllDnsCache() {
        final String host = "www.test_setDnsCache_getAllDnsCache.com";
        DnsCacheManipulator.setDnsCache(host, IP3);

        final List<DnsCacheEntry> allDnsCacheEntries = DnsCacheManipulator.getAllDnsCache();
        @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
        final List<DnsCacheEntry> expected = Arrays.asList(
                new DnsCacheEntry(host.toLowerCase(), new String[]{IP3}, new Date(Long.MAX_VALUE)));

        assertEquals(expected.size(), allDnsCacheEntries.size());

        DnsCacheEntry expectedDnsCacheEntry = expected.get(0);
        DnsCacheEntry dnsCacheEntry = allDnsCacheEntries.get(0);
        assertEquals(expectedDnsCacheEntry.getHost().toLowerCase(), dnsCacheEntry.getHost().toLowerCase());
        assertEquals(expectedDnsCacheEntry.getIp(), dnsCacheEntry.getIp());

        long now = currentTimeMillis();
        assertEquals(expectedDnsCacheEntry.getExpiration().getTime() - now > 315360000000L, dnsCacheEntry.getExpiration().getTime() - now > 315360000000L);
        assertTrue(DnsCacheManipulator.getWholeDnsCache().getNegativeCache().isEmpty());
    }

    @Test
    public void test_canSetExistedDomain_canExpire_thenReLookupBack() throws Exception {
        final String domain = "github.com";

        Set<String> expected = getAllHostAddresses(domain);

        DnsCacheManipulator.setDnsCache(30, domain, IP3);
        assertEquals(IP3, InetAddress.getByName(domain).getHostAddress());

        sleep(32);

        assertEquals(expected, getAllHostAddresses(domain));
    }

    private static Set<String> getAllHostAddresses(String domain) throws Exception {
        final InetAddress[] allByName = InetAddress.getAllByName(domain);
        Set<String> all = new HashSet<String>();
        for (InetAddress inetAddress : allByName) {
            all.add(inetAddress.getHostAddress());
        }
        return all;
    }

    @Test
    public void test_setNotExistedDomain_RemoveThenReLookupAndNotExisted() throws Exception {
        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        DnsCacheManipulator.setDnsCache(DOMAIN_NOT_EXISTED, IP3);

        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final String ip = InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
        assertEquals(IP3, ip);

        DnsCacheManipulator.removeDnsCache(DOMAIN_NOT_EXISTED);

        assertDomainNotExisted();


        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEquals(DOMAIN_NOT_EXISTED.toLowerCase(), negativeCache.get(0).getHost().toLowerCase());
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    private static void assertDomainNotExisted() {
        try {
            InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected);
            assertTrue(true);
        }
    }

    @Test
    public void test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted() throws Exception {
        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        DnsCacheManipulator.setDnsCache(100, DOMAIN_NOT_EXISTED, IP3);

        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final String ip = InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
        assertEquals(IP3, ip);

        sleep(100);

        assertDomainNotExisted();

        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEquals(DOMAIN_NOT_EXISTED.toLowerCase(), negativeCache.get(0).getHost().toLowerCase());
    }

    @Test
    public void test_multi_ips_in_config_file() {
        long now = currentTimeMillis();
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry entry = new DnsCacheEntry(host,
                new String[]{"42.42.41.1", "42.42.41.2"}, new Date(Long.MAX_VALUE));
        DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(host);
        assertEquals(entry.getHost(), dnsCache.getHost());
        assertEquals(entry.getIp(), dnsCache.getIp());
        assertEquals(entry.getExpiration().getTime() - now > 315360000000L, dnsCache.getExpiration().getTime() - now > 315360000000L);


        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry entryLoose = new DnsCacheEntry(hostLoose,
                new String[]{"42.42.41.1", "42.42.41.2", "42.42.41.3", "42.42.41.4"}, new Date(Long.MAX_VALUE));
        DnsCacheEntry dnsCacheLoose = DnsCacheManipulator.getDnsCache(hostLoose);
        assertEquals(entryLoose.getHost(), dnsCacheLoose.getHost());
        assertEquals(entryLoose.getIp(), dnsCacheLoose.getIp());
        assertEquals(entryLoose.getExpiration().getTime() - now > 315360000000L, dnsCacheLoose.getExpiration().getTime() - now > 315360000000L);

    }

    @Test
    public void test_nullSafeForGetDnsCache() {
        final DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(DOMAIN_NOT_EXISTED);
        assertNull(dnsCache);
    }

    @Test
    public void test_setDnsCachePolicy() throws Exception {
        final String host = "bing.com";
        DnsCacheManipulator.setDnsCachePolicy(2);
        assertEquals(2, DnsCacheManipulator.getDnsCachePolicy());

        InetAddress.getByName(host).getHostAddress();
        final long tick = currentTimeMillis();

        sleep(1000);
        InetAddress.getByName(host).getHostAddress();

        final DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(host);
        assertBetween(dnsCache.getExpiration().getTime(), tick, tick + 2020);

        sleep(1020);

        // return expired entry, because of no dns cache touch by external related operation!
        final DnsCacheEntry next = DnsCacheManipulator.getDnsCache(host);
        assertNotSame(dnsCache, next);
        assertEquals(dnsCache, next);

        // touch dns cache with external other host operation
        InetAddress.getByName("www.bing.com").getHostAddress();
        assertNull(DnsCacheManipulator.getDnsCache(host));

        // relookup
        InetAddress.getByName(host).getHostAddress();
        final DnsCacheEntry relookup = DnsCacheManipulator.getDnsCache(host);
        final long relookupTick = currentTimeMillis();
        assertBetween(relookup.getExpiration().getTime(), relookupTick, relookupTick + 2020);
    }

    @Test
    public void test_setNegativeDnsCachePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(2);
        assertEquals(2, DnsCacheManipulator.getDnsNegativeCachePolicy());

        try {
            InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
        final long tick = currentTimeMillis();

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, negativeCache.size());
        final DnsCacheEntry dnsCache = negativeCache.get(0);
        assertBetween(dnsCache.getExpiration().getTime(), tick, tick + 2020);

        sleep(1000);
        try {
            InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
        assertEquals(dnsCache, DnsCacheManipulator.getWholeDnsCache().getNegativeCache().get(0));

        sleep(1001);
        try {
            InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
        final long relookupTick = currentTimeMillis();
        final List<DnsCacheEntry> relookupNegativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, relookupNegativeCache.size());
        final DnsCacheEntry relookup = relookupNegativeCache.get(0);
        assertBetween(relookup.getExpiration().getTime(), relookupTick, relookupTick + 2020);
    }

    static void assertBetween(long actual, long start, long end) {
        final boolean ok = (start <= actual) && (actual <= end);
        if (!ok) {
            fail(start + " <= " + actual + " <= " + end + ", failed!");
        }
    }
}
