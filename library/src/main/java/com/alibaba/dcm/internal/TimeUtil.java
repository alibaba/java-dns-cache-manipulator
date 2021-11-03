package com.alibaba.dcm.internal;

/**
 * internal time util.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
final class TimeUtil {
    /**
     * record point of {@link System#currentTimeMillis()} for {@link #NANO_TIME_CHECK_POINT}
     */
    private static final long TIME_MILLIS_CHECK_POINT = System.currentTimeMillis();

    /**
     * record point of {@link System#nanoTime()} for {@link #TIME_MILLIS_CHECK_POINT}
     */
    private static final long NANO_TIME_CHECK_POINT = System.nanoTime();

    private static final long NS_PER_MS = 1000000;

    /**
     * @see <a href="https://newbedev.com/how-can-i-convert-the-result-of-system-nanotime-to-a-date-in-java">
     * How can I convert the result of System.nanoTime to a date in Java?</a>
     */
    public static long convertNanoTimeToTimeMillis(long nanoTime) {
        return (nanoTime - NANO_TIME_CHECK_POINT) / NS_PER_MS + TIME_MILLIS_CHECK_POINT;
    }

    public static long getNanoTimeAfterMs(long millSeconds) {
        return System.nanoTime() + millSeconds * NS_PER_MS;
    }

    private TimeUtil() {
    }
}
