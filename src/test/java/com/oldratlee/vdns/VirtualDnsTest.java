package com.oldratlee.vdns;

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
public class VirtualDnsTest {
    @Test
    public void test_getAllVirtualDns() throws Exception {
        VirtualDns.clearDnsCacheEntry();

        VirtualDns.setVirtualDns("www.test_getAllVirtualDns.com", "42.42.42.42");

        final List<DnsCacheEntry> allVirtualDns = VirtualDns.getAllDnsCacheEntry();
        final List<DnsCacheEntry> expected = Arrays.asList(
                new DnsCacheEntry("www.test_getAllVirtualDns.com".toLowerCase(),
                        "42.42.42.42",
                        new Date(Long.MAX_VALUE)));

        assertEquals(expected, allVirtualDns);
    }

    @Test
    public void test_configVirtualDnsByClassPathProperties() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties();
        final String ip = InetAddress.getByName("www.hello1.com").getHostAddress();
        assertEquals("42.42.41.41", ip);
    }

    @Test
    public void test_configVirtualDnsByMyProperties() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties("my-vdns.properties");
        final String ip = InetAddress.getByName("www.hello1.com").getHostAddress();
        assertEquals("42.42.43.43", ip);
    }

    @Test
    public void test_configNotFound() throws Exception {
        try {
            VirtualDns.configVirtualDnsByClassPathProperties("not-existed.properties");
            fail();
        } catch (VirtualDnsException expected) {
            assertEquals("Fail to find not-existed.properties on classpath!", expected.getMessage());
        }
    }

    @Test
    public void test_virtualDnsExpirationEffective() throws Exception {
        final String notExistedHost = "www.not-existed-host-test_virtualDnsExpirationEffective.com";

        VirtualDns.setVirtualDns(500, notExistedHost, "42.42.43.43");
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals("42.42.43.43", ip);

        Thread.sleep(1000);

        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }

    @Test
    public void test_removeVirtualDns() throws Exception {
        final String notExistedHost = "www.not-existed-host-test_removeVirtualDns.com";

        VirtualDns.setVirtualDns(notExistedHost, "42.42.43.43");
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals("42.42.43.43", ip);

        VirtualDns.removeVirtualDns(notExistedHost);
        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected.toString());
            assertTrue(true);
        }
    }
}
