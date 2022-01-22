package com.alibaba.dcm.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.net.InetAddress

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class IpParserUtilTests : FunSpec({

    test("ipv4 to ByteArray") {
        IpParserUtil.ip2ByteArray("192.168.0.13") shouldBe byteArrayOf(192.toByte(), 168.toByte(), 0.toByte(), 13)
        IpParserUtil.ip2ByteArray("10.192.255.0") shouldBe byteArrayOf(10, 192.toByte(), 255.toByte(), 0)

        val ip = "10.1.1.1"
        val actualIpBytes = IpParserUtil.ip2ByteArray(ip)

        actualIpBytes shouldBe byteArrayOf(10, 1, 1, 1)
        actualIpBytes shouldBe getIpByteArrayByGetAllByName(ip)
    }

    test("ipv6 to ByteArray") {
        val ip = "2404:6800:4005:80a:0:0:0:200e"
        val bytes = IpParserUtil.ip2ByteArray(ip)

        bytes shouldBe getIpByteArrayByGetAllByName(ip)
    }

    mapOf(
            "a.1.1.1" to "ip to ByteArray: ipv4 with char exception",
            "-2.168.0.13" to "ip to ByteArray: ipv4 minus exception",
            "1.1.1.256" to "ip to ByteArray: ipv4 overflow exception",
            "192.168.0.13.1" to "ip to ByteArray: ipv4 too long exception",
            "2404:6800:4005:80a:0:0:0:200z" to "ip to ByteArray: ipv6 with char exception",
            "-2404:6800:4005:80a:0:0:0:200e" to "ip to ByteArray: ipv6 minus exception",
            "2404:6800:4005:80a:0:0:0:200:123" to "ip to ByteArray: ipv6 too long exception",
    ).forEach { (ip, caseName) ->

        test("test $caseName") {
            shouldThrow<IllegalArgumentException> {
                IpParserUtil.ip2ByteArray(ip)
            }.message shouldBe ip + INVALID_IP_ADDRESS
        }

    }
})

private fun getIpByteArrayByGetAllByName(ip: String): ByteArray {
    val addresses = InetAddress.getAllByName(ip)
    addresses.shouldHaveSize(1)

    return addresses.first().address
}

private const val INVALID_IP_ADDRESS = ": invalid IP address"
