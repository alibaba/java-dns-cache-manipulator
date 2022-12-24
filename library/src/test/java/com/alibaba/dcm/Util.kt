package com.alibaba.dcm

import com.alibaba.dcm.internal.InetAddressCacheUtilCommons.isInetAddressImplOld
import com.alibaba.dcm.internal.TestTimeUtil.NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun String.lookupIpByName(): String =
    InetAddress.getByName(this).hostAddress

fun String.lookupAllIps(): List<String> =
    InetAddress.getAllByName(this).map { it.hostAddress }

fun skipOsLookupTimeAfterThenClear(vararg domains: String) {
    for (domain in domains) {
        // trigger dns cache by lookup and clear, skip OS lookup time after
        domain.lookupIpByName()
    }
    DnsCacheManipulator.clearDnsCache()
}

fun String.shouldBeNotExistedDomain() {
    shouldThrow<UnknownHostException> {
        this.lookupIpByName()
    }
}

fun shouldContainOnlyOneNegativeCacheWitchExpirationBetween(start: Long, end: Long) {
    val negativeCache = DnsCacheManipulator.listDnsNegativeCache()
    negativeCache.size shouldBe 1

    negativeCache.first().expiration.time.shouldBeBetween(start, end)
}

infix fun DnsCacheEntry?.shouldBeEqual(expected: DnsCacheEntry?) {
    if (this == null) {
        fail("actual DnsCacheEntry is null")
    }
    if (expected == null) {
        fail("expected DnsCacheEntry is null")
    }

    host shouldBeEqualAsHostName expected.host
    ips shouldBe expected.ips
    expiration.time shouldBeEqualAsExpiration expected.expiration.time
}

infix fun String.shouldBeEqualAsHostName(expected: String) {
    if (isInetAddressImplOld()) {
        // hard-coded test logic for jdk 8-
        //   host name is unified to lower case by InetAddress
        this shouldBeEqualIgnoringCase expected
    } else {
        this shouldBe expected
    }
}

private infix fun Long.shouldBeEqualAsExpiration(expected: Long) {
    if (expected == Long.MAX_VALUE) {
        if (isInetAddressImplOld()) {
            this shouldBe expected
        } else {
            // hard-coded test logic for jdk 9+
            this.shouldBeEqualsWithTolerance(NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS, 5)
        }
    } else {
        this shouldBe expected
    }
}

private fun Long.shouldBeEqualsWithTolerance(expected: Long, tolerance: Long) {
    this.shouldBeBetween(expected - tolerance, expected + tolerance)
}
