package com.alibaba.dcm.internal;

/**
 * internal time util.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 1.6.0
 */
class TimeUtil {
    /**
     * record point of {@link System#currentTimeMillis()} for {@link #NANO_TIME_CHECK_POINT}
     */
    private static final long TIME_MILLIS_CHECK_POINT = System.currentTimeMillis();

    /**
     * record point of {@link System#nanoTime()} for {@link #TIME_MILLIS_CHECK_POINT}
     */
    private static final long NANO_TIME_CHECK_POINT = System.nanoTime();

    private static final long MS_TO_NS = 1000000;

    public static long convertNanoTimeToTimeMillis(long nanoTime) {
        return (nanoTime - NANO_TIME_CHECK_POINT) / MS_TO_NS + TIME_MILLIS_CHECK_POINT;
    }

    public static long getNanoTimeAfterMs(long millSeconds) {
        return System.nanoTime() + millSeconds * MS_TO_NS;
    }
}
