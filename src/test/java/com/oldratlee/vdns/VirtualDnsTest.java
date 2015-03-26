package com.oldratlee.vdns;

import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ding.lid
 */
public class VirtualDnsTest {
    @Test
    public void test_configVirtualDnsByClassPathProperties() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties();
        final String ip = InetAddress.getByName("www.hello-world.com").getHostAddress();
        assertEquals("42.42.41.41", ip);
    }

    @Test
    public void test_getAllVirtualDns() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties();
        final List<Host> allVirtualDns = VirtualDns.getAllVirtualDns();
        final List<Host> expected = Arrays.asList(new Host("www.hello-world.com", "42.42.41.41", new Date(Long.MAX_VALUE)));
        assertEquals(expected, allVirtualDns);
    }

    @Test(expected = VirtualDnsException.class)
    public void test_configNotFound() throws Exception {
        VirtualDns.configVirtualDnsByClassPathProperties("not-existed.properties");
    }
}
