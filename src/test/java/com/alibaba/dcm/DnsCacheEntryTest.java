package com.alibaba.dcm;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheEntryTest {
    @Test
    public void test_equals() throws Exception {
        Date date = new Date(System.currentTimeMillis() + 1000 * 60);
        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, date);
        DnsCacheEntry entry2 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, date);
        assertEquals(entry1, entry2);

        DnsCacheEntry entryIps1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, date);
        DnsCacheEntry entryIps2 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, date);
        assertEquals(entryIps1, entryIps2);
    }

    @Test
    public void test_notEquals() throws Exception {
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
    public void test_getIp() throws Exception {
        DnsCacheEntry entry1 = new DnsCacheEntry("a.com", new String[]{"1.1.1.1"}, new Date());
        assertEquals("1.1.1.1", entry1.getIp());
        assertArrayEquals(new String[]{"1.1.1.1"}, entry1.getIps());

        DnsCacheEntry entryIps = new DnsCacheEntry("a.com", new String[]{"1.1.1.1", "2.2.2.2"}, new Date());
        assertEquals("1.1.1.1", entryIps.getIp());
        assertArrayEquals(new String[]{"1.1.1.1", "2.2.2.2"}, entryIps.getIps());
    }
}
