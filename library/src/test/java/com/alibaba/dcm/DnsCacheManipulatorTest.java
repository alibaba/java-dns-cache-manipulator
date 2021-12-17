package com.alibaba.dcm;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.alibaba.dcm.Util.*;
import static com.alibaba.dcm.internal.JavaVersionUtil.isJdkAtMost8;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
@SuppressWarnings("ConstantConditions")
public class DnsCacheManipulatorTest {
    private static final String IP1 = "42.42.42.1";
    private static final String IP2 = "42.42.42.2";
    private static final String IP3 = "42.42.42.3";
    private static final String IP4 = "42.42.42.4";

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

        assertEquals(IP1, lookupIpByName(host));
        assertEquals(Collections.singletonList(IP1), lookupAllIps(host));
    }

    @Test
    public void test_setDnsCache_multiIp() throws Exception {
        final String host = "multi.ip.com";
        final String[] ips = {IP1, IP2};
        DnsCacheManipulator.setDnsCache(host, ips);

        assertEquals(ips[0], lookupIpByName(host));
        assertEquals(Arrays.asList(ips), lookupAllIps(host));
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

        assertEqualsDnsCacheEntry(expected, actual);

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
        List<String> expected = lookupAllIps(EXISTED_DOMAIN);

        DnsCacheManipulator.setDnsCache(20, EXISTED_DOMAIN, IP1);
        assertEquals(IP1, lookupIpByName(EXISTED_DOMAIN));

        sleep(40);

        assertEquals(expected, lookupAllIps(EXISTED_DOMAIN));
    }

    @Test
    public void test_setNotExistedDomain_RemoveThenReLookupAndNotExisted() throws Exception {
        DnsCacheManipulator.setDnsCache(DOMAIN_NOT_EXISTED, IP1);
        assertEquals(IP1, lookupIpByName(DOMAIN_NOT_EXISTED));

        DnsCacheManipulator.removeDnsCache(DOMAIN_NOT_EXISTED);
        assertDomainNotExisted(DOMAIN_NOT_EXISTED);
        assertTrue(DnsCacheManipulator.listDnsCache().isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.listDnsNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsHostName(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
    }

    @Test
    public void test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted() throws Exception {
        DnsCacheManipulator.setDnsCache(20, DOMAIN_NOT_EXISTED, IP1);

        final String ip = lookupIpByName(DOMAIN_NOT_EXISTED);
        assertEquals(IP1, ip);

        sleep(40);
        assertDomainNotExisted(DOMAIN_NOT_EXISTED);

        assertTrue(DnsCacheManipulator.listDnsCache().isEmpty());

        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.listDnsNegativeCache();
        assertEquals(1, negativeCache.size());
        assertEqualsHostName(DOMAIN_NOT_EXISTED, negativeCache.get(0).getHost());
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

        DnsCacheManipulator.setDnsCachePolicy(1);
        assertEquals(1, DnsCacheManipulator.getDnsCachePolicy());

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        lookupIpByName(EXISTED_DOMAIN);
        final long tick = currentTimeMillis();
        DnsCacheEntry dnsCacheEntry = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN);
        assertBetween(dnsCacheEntry.getExpiration().getTime(),
                tick, tick + 1020);

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        sleep(500);
        lookupIpByName(EXISTED_DOMAIN);
        // get dns cache before expire
        assertEquals(dnsCacheEntry, DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        sleep(520);
        // return expired entry, because of no dns cache touch by external related operation!
        assertEquals(dnsCacheEntry, DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        lookupIpByName(EXISTED_ANOTHER_DOMAIN);
        assertNull(DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN));

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        lookupIpByName(EXISTED_DOMAIN);
        final long relookupTick = currentTimeMillis();
        // get dns cache after expire
        final DnsCacheEntry relookup = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN);
        assertBetween(relookup.getExpiration().getTime(),
                relookupTick, relookupTick + 1020);
    }

    @Test
    public void test_setNegativeDnsCachePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1);
        assertEquals(1, DnsCacheManipulator.getDnsNegativeCachePolicy());

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        assertLookupNotExisted(DOMAIN_NOT_EXISTED);
        final long tick = currentTimeMillis();
        assertOnlyNegativeCache(tick, tick + 1020);

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        sleep(500);
        assertLookupNotExisted(DOMAIN_NOT_EXISTED);
        // get dns cache before expire
        assertOnlyNegativeCache(tick, tick + 1020);

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        sleep(520);
        // get dns cache before expire
        assertOnlyNegativeCache(tick, tick + 1020);

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        lookupIpByName(EXISTED_DOMAIN);
        if (isJdkAtMost8()) {
            assertOnlyNegativeCache(tick, tick + 1020);
        } else {
            assertTrue(DnsCacheManipulator.listDnsNegativeCache().isEmpty());
        }

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        assertLookupNotExisted(DOMAIN_NOT_EXISTED);
        final long relookupTick = currentTimeMillis();
        assertOnlyNegativeCache(relookupTick, relookupTick + 1020);
    }

    ////////////////////////////////////////////////////////////////////
    // test for config file
    ////////////////////////////////////////////////////////////////////

    @Test
    public void test_loadDnsCacheConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig();
        final String ip = lookupIpByName("www.hello1.com");
        assertEquals(IP1, ip);
    }

    @Test
    public void test_loadDnsCacheConfig_from_D_Option() throws Exception {
        final String key = "dcm.config.filename";
        try {
            System.setProperty(key, "customized-dns-cache.properties");
            DnsCacheManipulator.loadDnsCacheConfig();

            final String ip = lookupIpByName("www.customized.com");
            assertEquals(IP2, ip);
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    public void test_loadDnsCacheConfig_fromMyConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
        final String ip = lookupIpByName("www.hello2.com");
        assertEquals(IP2, ip);
    }

    @Test
    public void test_multi_ips_in_config_file() {
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry expected = new DnsCacheEntry(host,
                new String[]{IP1, IP2}, Long.MAX_VALUE);

        final DnsCacheEntry actual = DnsCacheManipulator.getDnsCache(host);
        assertEqualsDnsCacheEntry(expected, actual);

        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry expectedLoose = new DnsCacheEntry(hostLoose,
                new String[]{IP1, IP2, IP3, IP4}, Long.MAX_VALUE);

        DnsCacheEntry actualLoose = DnsCacheManipulator.getDnsCache(hostLoose);
        assertEqualsDnsCacheEntry(expectedLoose, actualLoose);
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
