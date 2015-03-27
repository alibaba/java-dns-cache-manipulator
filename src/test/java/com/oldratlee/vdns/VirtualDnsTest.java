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
    public void test_configVirtualDnsByClassPathProperties() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties();
        final String ip = InetAddress.getByName("www.hello1.com").getHostAddress();
        assertEquals("42.42.41.41", ip);
    }

    @Test
    public void test_getAllVirtualDns() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties();
        final List<Host> allVirtualDns = VirtualDns.getAllVirtualDns();
        final List<Host> expected = Arrays.asList(new Host("www.hello1.com", "42.42.41.41", new Date(Long.MAX_VALUE)));
        assertEquals(expected, allVirtualDns);
    }

    @Test
    public void test_VirtualDnsExpire() throws Exception {
        final String notExistedHost = "www.not-existed-host-4754jd-kr8m07d5-76jn54.com";

        VirtualDns.setVirtualDns(500, notExistedHost, "42.42.43.43");
        final String ip = InetAddress.getByName(notExistedHost).getHostAddress();
        assertEquals("42.42.43.43", ip);

        Thread.sleep(1000);

        try {
            InetAddress.getByName(notExistedHost).getHostAddress();
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
    }

    @Test(expected = VirtualDnsException.class)
    public void test_configNotFound() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties("not-existed.properties");
    }
}
