package com.alibaba.dcm

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DnsCacheTests : AnnotationSpec() {
    @Test
    fun test_equals() {
        val expiration = System.currentTimeMillis()
        val entry1 = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        val entry2 = DnsCacheEntry("b.com", arrayOf("1.1.1.2"), expiration)
        val entry3 = DnsCacheEntry("c.com", arrayOf("1.1.1.2"), expiration)
        val entry4 = DnsCacheEntry("d.com", arrayOf("1.1.1.2"), expiration)

        val dnsCache1 = DnsCache(
                listOf(entry1, entry2),
                listOf(entry3))
        val dnsCache2 = DnsCache(
                listOf(entry1, entry2),
                listOf(entry4))

        dnsCache1 shouldNotBe dnsCache2
    }

    @Test
    fun test_toString() {
        val expiration = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val date = dateFormat.format(Date(expiration))

        val entry1 = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        val entry2 = DnsCacheEntry("b.com", arrayOf("1.1.1.2"), expiration)
        val entry3 = DnsCacheEntry("c.com", arrayOf("1.1.1.2"), expiration)

        val dnsCache = DnsCache(
                listOf(entry1, entry2),
                listOf(entry3))

        val expected = String.format("DnsCache{cache=[DnsCacheEntry{host='a.com', ips=[1.1.1.1], expiration=%s}" +
                ", DnsCacheEntry{host='b.com', ips=[1.1.1.2], expiration=%<s}]" +
                ", negativeCache=[DnsCacheEntry{host='c.com', ips=[1.1.1.2], expiration=%<s}]}", date)
        dnsCache.toString() shouldBe expected
    }
}
