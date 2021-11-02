package com.alibaba.dcm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.dcm.internal.JavaVersionUtil.isJdkAtMost8;
import static com.alibaba.dcm.internal.TestTimeUtil.NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS;
import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class Util {
    static String lookupIpByName(String name) throws UnknownHostException {
        return InetAddress.getByName(name).getHostAddress();
    }

    static List<String> lookupAllIps(String domain) throws Exception {
        final InetAddress[] allByName = InetAddress.getAllByName(domain);
        List<String> all = new ArrayList<String>();
        for (InetAddress inetAddress : allByName) {
            all.add(inetAddress.getHostAddress());
        }
        return all;
    }

    static void skipOSLookupTimeAfterThenClear(String... domains) throws UnknownHostException {
        for (String domain : domains) {
            // trigger dns cache by lookup and clear, skip OS lookup time after
            lookupIpByName(domain);
        }
        DnsCacheManipulator.clearDnsCache();
    }

    static void assertLookupNotExisted(String domain) {
        try {
            lookupIpByName(domain);
            fail();
        } catch (UnknownHostException expected) {
            assertTrue(true);
        }
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    static void assertDomainNotExisted(String domain) {
        try {
            lookupIpByName(domain);
            fail();
        } catch (UnknownHostException expected) {
            System.out.println(expected);
            assertTrue(true);
        }
    }

    static void assertOnlyNegativeCache(long start, long end) {
        final List<DnsCacheEntry> negativeCache = DnsCacheManipulator.listDnsNegativeCache();
        assertEquals(1, negativeCache.size());
        final DnsCacheEntry first = negativeCache.get(0);
        assertBetween(first.getExpiration().getTime(), start, end);
    }

    static void assertEqualsIgnoreHostCase(DnsCacheEntry expected, DnsCacheEntry actual) {
        assertEqualsIgnoreCase(expected.getHost(), actual.getHost());
        assertArrayEquals(expected.getIps(), actual.getIps());

        final long expectedExpiration = expected.getExpiration().getTime();
        final long actualExpiration = actual.getExpiration().getTime();

        if (expectedExpiration == Long.MAX_VALUE) {
            if (isJdkAtMost8()) {
                assertEquals(expectedExpiration, actualExpiration);
            } else {
                // hard code test logic for jdk 9+
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
