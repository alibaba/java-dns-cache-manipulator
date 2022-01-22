package com.alibaba.dcm

import com.alibaba.dcm.Util.shouldBeNotExistedDomain
import com.alibaba.dcm.Util.shouldContainOnlyOneNegativeCacheWitchExpirationBetween
import com.alibaba.dcm.Util.lookupAllIps
import com.alibaba.dcm.Util.lookupIpByName
import com.alibaba.dcm.Util.shouldBeEqual
import com.alibaba.dcm.Util.shouldBeEqualAsHostName
import com.alibaba.dcm.Util.skipOsLookupTimeAfterThenClear
import com.alibaba.dcm.internal.JavaVersionUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs


private const val IP1 = "42.42.42.1"
private const val IP2 = "42.42.42.2"
private const val IP3 = "42.42.42.3"
private const val IP4 = "42.42.42.4"
private const val DOMAIN_NOT_EXISTED = "www.domain-not-existed-2D4B2C4E-61D5-46B3-81FA-3A975156D1AE.com"
private const val EXISTED_DOMAIN = "bing.com"
private const val EXISTED_ANOTHER_DOMAIN = "www.bing.com"


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DnsCacheManipulatorTests : AnnotationSpec() {
    @BeforeAll
    fun beforeAll() {
        // System Properties
        // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        System.out.printf("Env info:%njava home: %s%njdk version: %s%n",
                System.getProperty("java.home"),
                System.getProperty("java.version"))
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
    }

    @Before
    fun before() {
        DnsCacheManipulator.clearDnsCache()
        @Suppress("DEPRECATION")
        DnsCacheManipulator.getAllDnsCache().shouldBeEmpty()
        DnsCacheManipulator.listDnsNegativeCache().shouldBeEmpty()
    }

    ////////////////////////////////////////////////////////////////////
    // user case test
    ////////////////////////////////////////////////////////////////////

    @Test
    fun test_getDnsCache_null_ForNotExistedDomain() {
        val dnsCache = DnsCacheManipulator.getDnsCache(DOMAIN_NOT_EXISTED)
        dnsCache.shouldBeNull()
    }

    @Test
    fun test_getDnsCache_null_ForExistDomain_ButNotLookupYet() {
        val dnsCache = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN)
        dnsCache.shouldBeNull()
    }

    @Test
    fun test_setSingleIp() {
        val host = "single.ip.com"
        DnsCacheManipulator.setDnsCache(host, IP1)

        host.lookupIpByName() shouldBe IP1
        host.lookupAllIps().shouldContainExactly(IP1)
    }

    @Test
    fun test_setDnsCache_multiIp() {
        val host = "multi.ip.com"
        val ips = arrayOf(IP1, IP2)
        DnsCacheManipulator.setDnsCache(host, *ips)

        host.lookupIpByName() shouldBe ips.first()
        // relookup the entry order may change
        host.lookupAllIps().shouldContainExactlyInAnyOrder(*ips)
    }

    @Test
    fun test_setDnsCache_getAllDnsCache() {
        val host = "www.test_setDnsCache_getAllDnsCache.com"
        DnsCacheManipulator.setDnsCache(host, IP1)

        @Suppress("DEPRECATION")
        val allDnsCacheEntries = DnsCacheManipulator.getAllDnsCache()
        allDnsCacheEntries shouldHaveSize 1

        val expected = DnsCacheEntry(host, arrayOf(IP1), Long.MAX_VALUE)
        val actual = allDnsCacheEntries.first()

        actual shouldBeEqual expected

        val another = DnsCacheManipulator.getDnsCache(host)
        val another2 = DnsCacheManipulator.getDnsCache(host)
        // instance equals but NOT same
        another shouldBe actual
        another shouldNotBeSameInstanceAs actual
        another2 shouldBe another
        another2 shouldNotBeSameInstanceAs another

        // Check NegativeCache
        DnsCacheManipulator.listDnsNegativeCache().shouldBeEmpty()
    }

    @Test
    fun test_canSetExistedDomain_canExpire_thenReLookupBack() {
        val expected = EXISTED_DOMAIN.lookupAllIps()

        DnsCacheManipulator.setDnsCache(20, EXISTED_DOMAIN, IP1)
        EXISTED_DOMAIN.lookupIpByName() shouldBe IP1

        Thread.sleep(40)

        // relookup the entry order may change
        EXISTED_DOMAIN.lookupAllIps() shouldContainExactlyInAnyOrder expected
    }

    @Test
    fun test_setNotExistedDomain_RemoveThenReLookupAndNotExisted() {
        DnsCacheManipulator.setDnsCache(DOMAIN_NOT_EXISTED, IP1)
        DOMAIN_NOT_EXISTED.lookupIpByName() shouldBe IP1

        DnsCacheManipulator.removeDnsCache(DOMAIN_NOT_EXISTED)
        DOMAIN_NOT_EXISTED.shouldBeNotExistedDomain()
        DnsCacheManipulator.listDnsCache().shouldBeEmpty()

        val negativeCache = DnsCacheManipulator.listDnsNegativeCache()
        negativeCache shouldHaveSize 1
        negativeCache.first().host shouldBeEqualAsHostName DOMAIN_NOT_EXISTED
    }

    @Test
    fun test_setNotExistedDomain_canExpire_thenReLookupAndNotExisted() {
        DnsCacheManipulator.setDnsCache(20, DOMAIN_NOT_EXISTED, IP1)

        val ip = DOMAIN_NOT_EXISTED.lookupIpByName()
        ip shouldBe IP1

        Thread.sleep(40)
        DOMAIN_NOT_EXISTED.shouldBeNotExistedDomain()

        DnsCacheManipulator.listDnsCache().shouldBeEmpty()

        val negativeCache = DnsCacheManipulator.listDnsNegativeCache()
        negativeCache shouldHaveSize 1
        negativeCache.first().host shouldBeEqualAsHostName DOMAIN_NOT_EXISTED
    }

    ////////////////////////////////////////////////////////////////////
    // test for CachePolicy
    ////////////////////////////////////////////////////////////////////

    @Test
    fun test_setDnsCachePolicy() {
        // trigger dns cache by lookup and clear, skip OS lookup time after,
        // otherwise the lookup operation time may take seconds.
        //
        // so reduce the lookup operation time,
        // make below time-tracking test code more stability
        skipOsLookupTimeAfterThenClear(EXISTED_DOMAIN, EXISTED_ANOTHER_DOMAIN)

        DnsCacheManipulator.setDnsCachePolicy(1)
        DnsCacheManipulator.getDnsCachePolicy() shouldBe 1

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        EXISTED_DOMAIN.lookupIpByName()
        val tick = System.currentTimeMillis()
        val dnsCacheEntry = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN)
        dnsCacheEntry!!.expiration.time.shouldBeBetween(tick, tick + 1020)

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        Thread.sleep(500)
        EXISTED_DOMAIN.lookupIpByName()
        // get dns cache before expire
        DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN) shouldBe dnsCacheEntry

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        Thread.sleep(520)
        // return expired entry, because of no dns cache touch by external related operation!
        DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN) shouldBe dnsCacheEntry

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        EXISTED_ANOTHER_DOMAIN.lookupIpByName()
        DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN).shouldBeNull()

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        EXISTED_DOMAIN.lookupIpByName()
        val relookupTick = System.currentTimeMillis()
        // get dns cache after expire
        val relookup = DnsCacheManipulator.getDnsCache(EXISTED_DOMAIN)
        relookup!!.expiration.time.shouldBeBetween(relookupTick, relookupTick + 1020)
    }

    @Test
    fun test_setNegativeDnsCachePolicy() {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1)
        DnsCacheManipulator.getDnsNegativeCachePolicy() shouldBe 1

        //////////////////////////////////////////////////
        // 0. trigger dns cache by lookup
        //////////////////////////////////////////////////
        DOMAIN_NOT_EXISTED.shouldBeNotExistedDomain()
        val tick = System.currentTimeMillis()
        shouldContainOnlyOneNegativeCacheWitchExpirationBetween(tick, tick + 1020)

        //////////////////////////////////////////////////
        // 1. lookup before expire
        //////////////////////////////////////////////////
        Thread.sleep(500)
        DOMAIN_NOT_EXISTED.shouldBeNotExistedDomain()
        // get dns cache before expire
        shouldContainOnlyOneNegativeCacheWitchExpirationBetween(tick, tick + 1020)

        //////////////////////////////////////////////////
        // 2. get dns cache after expire
        //////////////////////////////////////////////////
        Thread.sleep(520)
        // get dns cache before expire
        shouldContainOnlyOneNegativeCacheWitchExpirationBetween(tick, tick + 1020)

        //////////////////////////////////////////////////
        // 3. touch dns cache with external other host operation
        //////////////////////////////////////////////////
        EXISTED_DOMAIN.lookupIpByName()
        if (JavaVersionUtil.isJavaVersionAtMost8()) {
            shouldContainOnlyOneNegativeCacheWitchExpirationBetween(tick, tick + 1020)
        } else {
            DnsCacheManipulator.listDnsNegativeCache().shouldBeEmpty()
        }

        //////////////////////////////////////////////////
        // 4. relookup
        //////////////////////////////////////////////////
        DOMAIN_NOT_EXISTED.shouldBeNotExistedDomain()
        val relookupTick = System.currentTimeMillis()
        shouldContainOnlyOneNegativeCacheWitchExpirationBetween(relookupTick, relookupTick + 1020)
    }

    ////////////////////////////////////////////////////////////////////
    // test for config file
    ////////////////////////////////////////////////////////////////////
    @Test
    fun test_loadDnsCacheConfig() {
        DnsCacheManipulator.loadDnsCacheConfig()
        val ip = "www.hello1.com".lookupIpByName()
        ip shouldBe IP1
    }

    @Test
    fun test_loadDnsCacheConfig_from_D_Option() {
        val key = "dcm.config.filename"
        try {
            System.setProperty(key, "customized-dns-cache.properties")
            DnsCacheManipulator.loadDnsCacheConfig()
            "www.customized.com".lookupIpByName() shouldBe IP2
        } finally {
            System.clearProperty(key)
        }
    }

    @Test
    fun test_loadDnsCacheConfig_fromMyConfig() {
        DnsCacheManipulator.loadDnsCacheConfig("my-dns-cache.properties")
        "www.hello2.com".lookupIpByName() shouldBe IP2
    }

    @Test
    fun test_multi_ips_in_config_file() {
        DnsCacheManipulator.loadDnsCacheConfig("dns-cache-multi-ips.properties")

        val host = "www.hello-multi-ips.com"
        val expected = DnsCacheEntry(host, arrayOf(IP1, IP2), Long.MAX_VALUE)

        val actual = DnsCacheManipulator.getDnsCache(host)
        actual shouldBeEqual expected

        val hostLoose = "www.hello-multi-ips-loose.com"
        val expectedLoose = DnsCacheEntry(hostLoose, arrayOf(IP1, IP2, IP3, IP4), Long.MAX_VALUE)

        val actualLoose = DnsCacheManipulator.getDnsCache(hostLoose)
        actualLoose shouldBeEqual expectedLoose
    }

    @Test
    fun test_configNotFound() {
        val ex = shouldThrow<DnsCacheManipulatorException> {
            DnsCacheManipulator.loadDnsCacheConfig("not-existed.properties")
        }

        ex.message shouldBe "Fail to find not-existed.properties on classpath!"
    }
}
