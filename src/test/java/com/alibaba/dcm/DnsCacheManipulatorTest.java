package com.alibaba.dcm;

import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author ding.lid
 */
public class DnsCacheManipulatorTest {
    static final String DOMAIN1 = "www.hello1.com";
    static final String IP1 = "42.42.41.41";
    static final String DOMAIN2 = "www.hello2.com";
    static final String IP2 = "42.42.41.42";
    public static final String IP3 = "42.42.43.43";

    static final String DOMAIN_NOT_EXISTED = "www.domain-not-existed-7352jt-12559-AZ-7524087.com";

    @BeforeClass
    public static void beforeClass() throws Exception {
        DnsCacheManipulator.clearDnsCache();
        assertEquals(0, DnsCacheManipulator.getAllDnsCache().size());
    }

    @Test
    public void test_loadDnsCacheConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig();
        final String ip = InetAddress.getByName(DOMAIN1).getHostAddress();
        assertEquals(IP1, ip);
    }

    @Test
    public void test_loadDnsCacheConfig_fromMyConfig() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties");
        final String ip = InetAddress.getByName(DOMAIN2).getHostAddress();
        assertEquals(IP2, ip);
    }

    @Test
    public void test_configNotFound() throws Exception {
        try {
            DnsCacheManipulator.loadDnsCacheConfig("not-existed.properties");
            fail();
        } catch (DnsCacheManipulatorException expected) {
            assertEquals("Fail to find not-existed.properties on classpath!", expected.getMessage());
        }
    }

    @Test
    public void test_setDnsCache_getAllDnsCache() throws Exception {
        final String host = "www.test_setDnsCache_getAllDnsCache.com";
        DnsCacheManipulator.setDnsCache(host, IP3);

        final List<DnsCacheEntry> allDnsCacheEntries = DnsCacheManipulator.getAllDnsCache();
        final List<DnsCacheEntry> expected = Arrays.asList(
                new DnsCacheEntry(host.toLowerCase(), new String[]{IP3}, new Date(Long.MAX_VALUE)));

        assertEquals(expected, allDnsCacheEntries);
    }

    @Test
    public void test_removeDnsCache() throws Exception {
        final String notExistedHost = "www.not-existed-host-test_removeDnsCache";

        DnsCacheManipulator.setDnsCache(notExistedHost, IP3);
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals(IP3, ip);

        DnsCacheManipulator.removeDnsCache(notExistedHost);

        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }

    @Test
    public void test_canResetExistedDomain_canExpire_thenReLookupBack() throws Exception {
        final String domain = "github.com";

        final String ip = InetAddress.getByName(domain).getHostAddress();

        DnsCacheManipulator.setDnsCache(30, domain, IP3);
        assertEquals(IP3, InetAddress.getByName(domain).getHostAddress());

        Thread.sleep(32);

        assertEquals(ip, InetAddress.getByName(domain).getHostAddress());
    }

    @Test
    public void test_DnsCache_setNotExistedDomain_canExpire_thenReLookupAndNotExisted() throws Exception {
        DnsCacheManipulator.setDnsCache(30, DOMAIN_NOT_EXISTED, IP3);
        final String ip = InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
        assertEquals(IP3, ip);

        Thread.sleep(32);

        try {
            InetAddress.getByName(DOMAIN_NOT_EXISTED).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }

    @Test
    public void test_multi_ips_in_config_file() throws Exception {
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties");

        final String host = "www.hello-multi-ips.com";
        DnsCacheEntry entry = new DnsCacheEntry(host,
                new String[]{"42.42.41.1", "42.42.41.2"}, new Date(Long.MAX_VALUE));
        assertEquals(entry, DnsCacheManipulator.getDnsCache(host));

        final String hostLoose = "www.hello-multi-ips-loose.com";
        DnsCacheEntry entryLoose = new DnsCacheEntry(hostLoose,
                new String[]{"42.42.41.1", "42.42.41.2", "42.42.41.3", "42.42.41.4"}, new Date(Long.MAX_VALUE));
        assertEquals(entryLoose, DnsCacheManipulator.getDnsCache(hostLoose));
    }
}
