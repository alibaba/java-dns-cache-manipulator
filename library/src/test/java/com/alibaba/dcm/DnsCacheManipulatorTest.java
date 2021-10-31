package com.alibaba.dcm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.alibaba.dcm.Util.getIpByName;
import static com.alibaba.dcm.internal.TestTimeUtil.NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS;
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

    private static final String DOMAIN_NOT_EXISTED = "www.domain-not-existed-2D4B2C4E-61D5-46B3-81FA-3A975156D1AE.com";

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
        final String ip = getIpByName(DOMAIN1);
        assertEquals(IP1, ip);
    }

    @Test
    public void test_loadDnsCacheConfig_from_D_Option() throws Exception {
        final String key = "dcm.config.filename";
        try {
            System.setProperty(key, "customized-dns-cache.properties");
            DnsCacheManipulator.loadDnsCacheConfig();

            final String ip = getIpByName(DOMAIN_CUSTOMIZED);
            assertEquals(IP_CUSTOMIZED, ip);
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    public void test_loadDnsCacheConfig_fromMyConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
        final String ip = getIpByName(DOMAIN2);
        assertEquals(IP2, ip);
    }

    @Test
    public void test_setMultiIp() throws Exception {
        DnsCacheManipulator.setDnsCache("multi.ip.com", "1.1.1.1", "2.2.2.2");
        String ip = getIpByName("multi.ip.com");
        assertEquals("1.1.1.1", ip);

        List<String> allIps = getAllIps("multi.ip.com");
        assertEquals(Arrays.asList("1.1.1.1", "2.2.2.2"), allIps);
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
        assertEquals(1, allDnsCacheEntries.size());

        final DnsCacheEntry expected = new DnsCacheEntry(
                host.toLowerCase(), new String[]{IP3}, new Date(Long.MAX_VALUE));

        assertEqualsIgnoreHostCase(expected, allDnsCacheEntries.get(0));

        // Check NegativeCache
        assertTrue(DnsCacheManipulator.getWholeDnsCache().getNegativeCache().isEmpty());
    }

    @Test
    public void test_canSetExistedDomain_canExpire_thenReLookupBack() throws Exception {
        final String domain = "github.com";

        List<String> expected = getAllIps(domain);

        DnsCacheManipulator.setDnsCache(30, domain, IP3);
        assertEquals(IP3, getIpByName(domain));

        sleep(32);

        assertEquals(expected, getAllIps(domain));
    }

    private static List<String> getAllIps(String domain) throws Exception {
        final InetAddress[] allByName = InetAddress.getAllByName(domain);
        List<String> all = new ArrayList<String>();
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
        final String ip = getIpByName(DOMAIN_NOT_EXISTED);
        assertEquals(IP3, ip);

        DnsCacheManipulator.removeDnsCache(DOMAIN_NOT_EXISTED);

        assertDomainNotExisted();


        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsIgnoreCase(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    private static void assertDomainNotExisted() {
        try {
            getIpByName(DOMAIN_NOT_EXISTED);
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
        final String ip = getIpByName(DOMAIN_NOT_EXISTED);
        assertEquals(IP3, ip);

        sleep(100);

        assertDomainNotExisted();

        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s\n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.getWholeDnsCache().getNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsIgnoreCase(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
    }

    @Test
    public void test_multi_ips_in_config_file() {
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry expected = new DnsCacheEntry(host,
                new String[]{"42.42.41.1", "42.42.41.2"}, new Date(Long.MAX_VALUE));

        final DnsCacheEntry actual = DnsCacheManipulator.getDnsCache(host);
        assertEqualsIgnoreHostCase(expected, actual);

        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry expectedLoose = new DnsCacheEntry(hostLoose,
                new String[]{"42.42.41.1", "42.42.41.2", "42.42.41.3", "42.42.41.4"}, new Date(Long.MAX_VALUE));

        DnsCacheEntry actualLoose = DnsCacheManipulator.getDnsCache(hostLoose);
        assertEqualsIgnoreHostCase(expectedLoose, actualLoose);
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

        getIpByName(host);
        final long tick = currentTimeMillis();

        sleep(1000);
        getIpByName(host);

        final DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(host);
        assertBetween(dnsCache.getExpiration().getTime(), tick, tick + 2020);

        sleep(1020);

        // return expired entry, because of no dns cache touch by external related operation!
        final DnsCacheEntry next = DnsCacheManipulator.getDnsCache(host);
        assertNotSame(dnsCache, next);
        assertEquals(dnsCache, next);

        // touch dns cache with external other host operation
        getIpByName("www.bing.com");
        assertNull(DnsCacheManipulator.getDnsCache(host));

        // relookup
        getIpByName(host);
        final DnsCacheEntry relookup = DnsCacheManipulator.getDnsCache(host);
        final long relookupTick = currentTimeMillis();
        assertBetween(relookup.getExpiration().getTime(), relookupTick, relookupTick + 2020);
    }

    @Test
    public void test_setNegativeDnsCachePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(2);
        assertEquals(2, DnsCacheManipulator.getDnsNegativeCachePolicy());

        try {
            getIpByName(DOMAIN_NOT_EXISTED);
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
            getIpByName(DOMAIN_NOT_EXISTED);
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
        assertEquals(dnsCache, DnsCacheManipulator.getWholeDnsCache().getNegativeCache().get(0));

        sleep(1001);
        try {
            getIpByName(DOMAIN_NOT_EXISTED);
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

    static void assertEqualsIgnoreHostCase(DnsCacheEntry expected, DnsCacheEntry actual) {
        assertEqualsIgnoreCase(expected.getHost(), actual.getHost());
        assertArrayEquals(expected.getIps(), actual.getIps());

        final long expectedExpiration = expected.getExpiration().getTime();
        final long actualExpiration = actual.getExpiration().getTime();
        if (expectedExpiration == Long.MAX_VALUE) {
            // hard code test logic for jdk 9+
            if (actualExpiration != Long.MAX_VALUE) {
                assertEqualsWithTolerance(NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS, actualExpiration, 5);
            }
        } else {
            assertEquals(expectedExpiration, actualExpiration);
        }
    }

    static void assertEqualsIgnoreCase(String expected, String actual) {
        assertEquals(expected.toLowerCase(), actual.toLowerCase());
    }

    static void assertBetween(long actual, long start, long end) {
        final boolean ok = (start <= actual) && (actual <= end);
        if (!ok) {
            fail(start + " <= " + actual + " <= " + end + ", failed!");
        }
    }

    static void assertEqualsWithTolerance(long expected, long actual, long tolerance) {
        assertBetween(actual, expected - tolerance, expected + tolerance);
    }
}
