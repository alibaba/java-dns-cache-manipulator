package com.alibaba.dcm;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheEntryTest {
    @Test
    public void test_equals() {
        Date date = new Date(System.currentTimeMillis() + 1000 * 60);
        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, date);
        DnsCacheEntry entry2 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, date);
        assertEquals(entry1, entry2);

        DnsCacheEntry entryIps1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, date);
        DnsCacheEntry entryIps2 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, date);
        assertEquals(entryIps1, entryIps2);
    }

    @Test
    public void test_notEquals() {
        Date date = new Date(System.currentTimeMillis() + 1000 * 60);

        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, date);
        DnsCacheEntry entry2 = new DnsCacheEntry("a.com", new String[]{"2.2.2.2"}, date);

        assertNotEquals(entry1, entry2);

        DnsCacheEntry entryNow = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, new Date());
        assertNotEquals(entry1, entryNow);

        DnsCacheEntry entryIps = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, date);
        assertNotEquals(entry1, entryIps);

        DnsCacheEntry entryDomainB = new DnsCacheEntry("b.com", new String[]{"1.1.1.1"}, date);
        assertNotEquals(entry1, entryDomainB);
    }

    @Test
    public void test_getter() {
        final Date expiration = new Date();
        DnsCacheEntry entry = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, expiration);
        assertEquals("a.com", entry.getHost());
        assertEquals("1.1.1.1", entry.getIp());

        assertArrayEquals(new String[]{"1.1.1.1"}, entry.getIps());
        assertNotSame(entry.getIps(), entry.getIps());
        assertArrayEquals(entry.getIps(), entry.getIps());
        assertNotSame(expiration, entry.getExpiration());

        DnsCacheEntry entryIps = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, expiration);
        assertEquals("1.1.1.1", entryIps.getIp());
        assertArrayEquals(new String[]{"1.1.1.1", "2.2.2.2"}, entryIps.getIps());
    }

    @Test
    public void test_toString() {
        final Date expiration = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        final String date = dateFormat.format(expiration);
        DnsCacheEntry entry = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, expiration);

        String expected = String.format("DnsCacheEntry{host='a.com', ips=[1.1.1.1], expiration=%s}", date);
        assertEquals(expected, entry.toString());
    }
}
