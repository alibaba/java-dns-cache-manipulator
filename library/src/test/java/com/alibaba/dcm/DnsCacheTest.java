package com.alibaba.dcm;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheTest {
    @Test
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void test_equals() {
        final Date expiration = new Date();
        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, expiration);
        DnsCacheEntry entry2 = new DnsCacheEntry("b.com", new String[]{"1.1.1.2"}, expiration);
        DnsCacheEntry entry3 = new DnsCacheEntry("c.com", new String[]{"1.1.1.2"}, expiration);
        DnsCacheEntry entry4 = new DnsCacheEntry("d.com", new String[]{"1.1.1.2"}, expiration);

        DnsCache dnsCache1 = new DnsCache(
                Arrays.asList(entry1, entry2),
                Arrays.asList(entry3));
        DnsCache dnsCache2 = new DnsCache(
                Arrays.asList(entry1, entry2),
                Arrays.asList(entry4));

        assertNotEquals(dnsCache1, dnsCache2);
    }

    @Test
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void test_toString() {
        final Date expiration = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String date = dateFormat.format(expiration);
        
        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, expiration);
        DnsCacheEntry entry2 = new DnsCacheEntry("b.com", new String[]{"1.1.1.2"}, expiration);
        DnsCacheEntry entry3 = new DnsCacheEntry("c.com", new String[]{"1.1.1.2"}, expiration);

        DnsCache dnsCache = new DnsCache(
                Arrays.asList(entry1, entry2),
                Arrays.asList(entry3));

        String expected = String.format("DnsCache{cache=[DnsCacheEntry{host='a.com', ips=[1.1.1.1], expiration=%s}" +
                ", DnsCacheEntry{host='b.com', ips=[1.1.1.2], expiration=%<s}]" +
                ", negativeCache=[DnsCacheEntry{host='c.com', ips=[1.1.1.2], expiration=%<s}]}", date);

        assertEquals(expected, dnsCache.toString());
    }
}
