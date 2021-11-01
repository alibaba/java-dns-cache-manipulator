package com.alibaba.dcm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.alibaba.dcm.Util.*;
import static com.alibaba.dcm.internal.JavaVersionUtil.isJdkAtMost8;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheManipulatorTest {
    private static final String IP1 = "42.42.42.1";
    private static final String IP2 = "42.42.42.2";
    private static final String IP_CUSTOMIZED = "42.42.42.42";

    private static final String DOMAIN1 = "www.hello1.com";
    private static final String DOMAIN2 = "www.hello2.com";
    private static final String DOMAIN_CUSTOMIZED = "www.customized.com";

    private static final String DOMAIN_NOT_EXISTED = "www.domain-not-existed-2D4B2C4E-61D5-46B3-81FA-3A975156D1AE.com";
    private static final String EXISTED_DOMAIN = "bing.com";
    private static final String EXISTED_ANOTHER_DOMAIN = "www.bing.com";

    @BeforeClass
    public static void beforeClass() {
        // System Properties
        // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.printf("Env info:%njava home: %s%njdk version: %s%n",
                System.getProperty("java.home"),
                System.getProperty("java.version"));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Before
    @SuppressWarnings("deprecation")
    public void before() {
        DnsCacheManipulator.clearDnsCache();
        assertTrue(DnsCacheManipulator.getAllDnsCache().isEmpty());
        assertTrue(DnsCacheManipulator.listDnsNegativeCache().isEmpty());
    }

    ////////////////////////////////////////////////////////////////////
    // user case test
    ////////////////////////////////////////////////////////////////////

    @Test
    public void test_getDnsCache_null_ForNotExistedDomain() {
        final DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(DOMAIN_NOT_EXISTED);
        assertNull(dnsCache);
    }

    @Test
    public void test_getDnsCache_null_ForExistDomain_ButNotLookupYet() {
        final DnsCacheEntry dnsCache = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN);
        assertNull(dnsCache);
    }

    @Test
    public void test_setSingleIp() throws Exception {
        final String host = "single.ip.com";
        DnsCacheManipulator.setDnsCache(host, IP1);

        assertEquals(IP1, getIpByName(host));
        assertEquals(Collections.singletonList(IP1), getAllIps(host));
    }

    @Test
    public void test_setDnsCache_multiIp() throws Exception {
        final String host = "multi.ip.com";
        final String[] ips = {IP1, IP2};
        DnsCacheManipulator.setDnsCache(host, ips);

        assertEquals(ips[0], getIpByName(host));
        assertEquals(Arrays.asList(ips), getAllIps(host));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void test_setDnsCache_getAllDnsCache() {
        final String host = "www.test_setDnsCache_getAllDnsCache.com";
        DnsCacheManipulator.setDnsCache(host, IP1);

        final List<DnsCacheEntry> allDnsCacheEntries = DnsCacheManipulator.getAllDnsCache();
        assertEquals(1, allDnsCacheEntries.size());

        final DnsCacheEntry expected = new DnsCacheEntry(
                host, new String[]{IP1}, Long.MAX_VALUE);
        DnsCacheEntry actual = allDnsCacheEntries.get(0);

        assertEqualsIgnoreHostCase(expected, actual);

        DnsCacheEntry another = DnsCacheManipulator.getDnsCache(host);
        DnsCacheEntry another2 = DnsCacheManipulator.getDnsCache(host);
        // instance equals but NOT same
        assertEquals(actual, another);
        assertNotSame(actual, another);
        assertEquals(another, another2);
        assertNotSame(another, another2);

        // Check NegativeCache
        assertTrue(DnsCacheManipulator.listDnsNegativeCache().isEmpty());
    }

    @Test
    public void test_canSetExistedDomain_canExpire_thenReLookupBack() throws Exception {
        final String domain = EXISTED_DOMAIN;
        List<String> expected = getAllIps(domain);

        DnsCacheManipulator.setDnsCache(20, domain, IP1);
        assertEquals(IP1, getIpByName(domain));

        sleep(30);

        assertEquals(expected, getAllIps(domain));
    }

    @Test
    public void test_setNotExistedDomain_RemoveThenReLookupAndNotExisted() throws Exception {
        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        DnsCacheManipulator.setDnsCache(DOMAIN_NOT_EXISTED, IP1);

        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final String ip = getIpByName(DOMAIN_NOT_EXISTED);
        assertEquals(IP1, ip);

        DnsCacheManipulator.removeDnsCache(DOMAIN_NOT_EXISTED);

        assertDomainNotExisted(DOMAIN_NOT_EXISTED);

        System.out.printf("%s(%s) test_setNotExistedDomain_RemoveThenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.listDnsNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsIgnoreCase(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
    }

    @Test
    public void test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted() throws Exception {
        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        DnsCacheManipulator.setDnsCache(50, DOMAIN_NOT_EXISTED, IP1);

        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final String ip = getIpByName(DOMAIN_NOT_EXISTED);
        assertEquals(IP1, ip);

        sleep(100);
        assertDomainNotExisted(DOMAIN_NOT_EXISTED);

        System.out.printf("%s(%s) test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted %s%n",
                new Date(), currentTimeMillis(), DnsCacheManipulator.getWholeDnsCache());
        final List<DnsCacheEntry> cache = DnsCacheManipulator.listDnsCache();
        assertTrue(cache.isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.listDnsNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsIgnoreCase(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
    }

    ////////////////////////////////////////////////////////////////////
    // test for CachePolicy
    ////////////////////////////////////////////////////////////////////

    @Test
    public void test_setDnsCachePolicy() throws Exception {
        // trigger dns cache by lookup and clear, skip OS lookup time after,
        // otherwise the lookup operation time may take seconds.
        //
        // so reduce the lookup operation time,
        // make below time-tracking test code more stability
        skipOSLookupTimeAfterThenClear(EXISTED_DOMAIN, EXISTED_ANOTHER_DOMAIN);

        DnsCacheManipulator.setDnsCachePolicy(2);
        assertEquals(2, DnsCacheManipulator.getDnsCachePolicy());

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        getIpByName(EXISTED_DOMAIN);
        final long tick = currentTimeMillis();
        DnsCacheEntry dnsCacheEntry = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN);
        assertBetween(dnsCacheEntry.getExpiration().getTime(),
                tick, tick + 2020);

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        sleep(1000);
        getIpByName(EXISTED_DOMAIN);
        // get dns cache before expire
        assertEquals(dnsCacheEntry, DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        sleep(1020);
        // return expired entry, because of no dns cache touch by external related operation!
        assertEquals(dnsCacheEntry, DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        getIpByName(EXISTED_ANOTHER_DOMAIN);
        assertNull(DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        getIpByName(EXISTED_DOMAIN);
        final long relookupTick = currentTimeMillis();
        // get dns cache after expire
        final DnsCacheEntry relookup = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN);
        assertBetween(relookup.getExpiration().getTime(),
                relookupTick, relookupTick + 2020);
    }

    @Test
    public void test_setNegativeDnsCachePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(2);
        assertEquals(2, DnsCacheManipulator.getDnsNegativeCachePolicy());

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        lookupNotExisted(DOMAIN_NOT_EXISTED);
        final long tick = currentTimeMillis();
        assertOnlyNegativeCache(tick, tick + 2020);

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        sleep(1000);
        lookupNotExisted(DOMAIN_NOT_EXISTED);
        // get dns cache before expire
        assertOnlyNegativeCache(tick, tick + 2020);

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        sleep(1020);
        // get dns cache before expire
        assertOnlyNegativeCache(tick, tick + 2020);

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        getIpByName(EXISTED_DOMAIN);
        if (isJdkAtMost8()) {
            assertOnlyNegativeCache(tick, tick + 2020);
        } else {
            assertTrue(DnsCacheManipulator.listDnsNegativeCache().isEmpty());
        }

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        lookupNotExisted(DOMAIN_NOT_EXISTED);
        final long relookupTick = currentTimeMillis();
        assertOnlyNegativeCache(relookupTick, relookupTick + 2020);
    }

    ////////////////////////////////////////////////////////////////////
    // test for config file
    ////////////////////////////////////////////////////////////////////

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
    public void test_multi_ips_in_config_file() {
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry expected = new DnsCacheEntry(host,
                new String[]{"42.42.41.1", "42.42.41.2"}, Long.MAX_VALUE);

        final DnsCacheEntry actual = DnsCacheManipulator.getDnsCache(host);
        assertEqualsIgnoreHostCase(expected, actual);

        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry expectedLoose = new DnsCacheEntry(hostLoose,
                new String[]{"42.42.41.1", "42.42.41.2", "42.42.41.3", "42.42.41.4"}, Long.MAX_VALUE);

        DnsCacheEntry actualLoose = DnsCacheManipulator.getDnsCache(hostLoose);
        assertEqualsIgnoreHostCase(expectedLoose, actualLoose);
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

}
