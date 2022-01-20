package com.alibaba.dcm

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import java.text.SimpleDateFormat

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DnsCacheEntryTests : AnnotationSpec() {
    @Test
    fun test_equals() {
        val expiration = System.currentTimeMillis() + 1000 * 60
        val entry1 = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        val entry2 = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        entry1 shouldBe entry2

        val entryIps1 = DnsCacheEntry("a.com", arrayOf("1.1.1.1", "2.2.2.2"), expiration)
        val entryIps2 = DnsCacheEntry("a.com", arrayOf("1.1.1.1", "2.2.2.2"), expiration)
        entryIps1 shouldBe entryIps2
    }

    @Test
    fun test_notEquals() {
        val expiration = System.currentTimeMillis() + 1000 * 60

        val entry1 = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        val entry2 = DnsCacheEntry("a.com", arrayOf("2.2.2.2"), expiration)
        entry2 shouldNotBe entry1

        val entryNow = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), System.currentTimeMillis())
        entryNow shouldNotBe entry1

        val entryIps = DnsCacheEntry("a.com", arrayOf("1.1.1.1", "2.2.2.2"), expiration)
        entryIps shouldNotBe entry1

        val entryDomainB = DnsCacheEntry("b.com", arrayOf("1.1.1.1"), expiration)
        entryDomainB shouldNotBe entry1
    }

    @Test
    fun test_getter() {
        val expiration = System.currentTimeMillis()
        val entry = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        entry.host shouldBe "a.com"
        entry.ip shouldBe "1.1.1.1"

        entry.ips.shouldContainExactly("1.1.1.1")
        entry.ips shouldNotBeSameInstanceAs entry.ips
        entry.ips shouldBe entry.ips

        val entryIps = DnsCacheEntry("a.com", arrayOf("1.1.1.1", "2.2.2.2"), expiration)
        entryIps.ip shouldBe "1.1.1.1"
        entryIps.ips.shouldContainExactly("1.1.1.1", "2.2.2.2")
    }

    @Test
    fun test_toString() {
        val expiration = System.currentTimeMillis()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dateString = dateFormat.format(expiration)
        val expected = String.format("DnsCacheEntry{host='a.com', ips=[1.1.1.1], expiration=%s}", dateString)

        val actual = DnsCacheEntry("a.com", arrayOf("1.1.1.1"), expiration)
        actual.toString() shouldBe expected
    }
}
