package com.alibaba.dcm.internal;

import static com.alibaba.dcm.internal.InetAddressCacheUtilCommons.NEVER_EXPIRATION;

public class TestTimeUtil {
    public final static long NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS =
            TimeUtil.convertNanoTimeToTimeMillis(NEVER_EXPIRATION);
}
