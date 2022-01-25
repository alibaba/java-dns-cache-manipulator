package com.alibaba.dcm

import com.alibaba.dcm.internal.JavaVersionUtil.isJavaVersionAtMost8
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
fun String.lookupIpByName(): String {
    return InetAddress.getByName(this).hostAddress
}

fun String.lookupAllIps(): List<String> {
    val allByName = InetAddress.getAllByName(this)
    val all: MutableList<String> = ArrayList()
    for (inetAddress in allByName) {
        all.add(inetAddress.hostAddress)
    }
    return all
}

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

    val expectedExpiration = expected.expiration.time
    val actualExpiration = expiration.time

    if (expectedExpiration == Long.MAX_VALUE) {
        if (isJavaVersionAtMost8()) {
            actualExpiration shouldBe expectedExpiration
        } else {
            // hard code test logic for jdk 9+
            actualExpiration.shouldBeEqualsWithTolerance(NEVER_EXPIRATION_NANO_TIME_TO_TIME_MILLIS, 5)
        }
    } else {
        actualExpiration shouldBe expectedExpiration
    }
}

infix fun String.shouldBeEqualAsHostName(expected: String) {
    if (isJavaVersionAtMost8()) {
        // java 8-, host name is unified to lower case by InetAddress
        this shouldBeEqualIgnoringCase expected
    } else {
        this shouldBe expected
    }
}

private fun Long.shouldBeEqualsWithTolerance(expected: Long, tolerance: Long) {
    this.shouldBeBetween(expected - tolerance, expected + tolerance)
}
