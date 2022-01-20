package com.alibaba.dcm.agent

import com.alibaba.dcm.DnsCacheManipulator
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.apache.commons.io.FileUtils
import java.io.File


private const val UTF8 = "UTF-8"


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DcmAgentTests : AnnotationSpec() {
    private lateinit var outputFile: File
    private lateinit var outputFilePath: String

    @Before
    fun setUp() {
        outputFile = File.createTempFile("dcm-output-", ".log")
        outputFile.length() shouldBe 0
        println("Prepared output file: " + outputFile.absolutePath)

        outputFilePath = outputFile.absolutePath

        DnsCacheManipulator.clearDnsCache()
    }

    @Test
    fun test_agentmain_empty() {
        DcmAgent.agentmain("   ")
    }

    @Test
    fun test_agentmain_file() {
        DcmAgent.agentmain("file $outputFilePath")

        val content = FileUtils.readLines(outputFile, UTF8)
        content.first() shouldContain "No action in agent argument, do nothing!"
    }

    @Test
    fun test_agentmain_set() {
        DcmAgent.agentmain("set bing.com 1.2.3.4")

        DnsCacheManipulator.getDnsCache("bing.com")!!.ip shouldBe "1.2.3.4"
    }

    @Test
    fun test_agentmain_set_toFile() {
        DcmAgent.agentmain("set bing.com 1.2.3.4 file $outputFilePath")

        DnsCacheManipulator.getDnsCache("bing.com")!!.ip shouldBe "1.2.3.4"

        val content = FileUtils.readLines(outputFile, UTF8)
        content.last() shouldBe DcmAgent.DCM_AGENT_SUCCESS_MARK_LINE
    }

    @Test
    fun test_agentmain_set_MultiIp() {
        DcmAgent.agentmain("set bing.com 1.1.1.1 2.2.2.2")

        DnsCacheManipulator.getDnsCache("bing.com")!!.ips.shouldContainExactly("1.1.1.1", "2.2.2.2")
    }

    @Test
    fun test_agentmain_get() {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3")

        DcmAgent.agentmain("get bing.com")
    }

    @Test
    fun test_agentmain_rm() {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3")

        DcmAgent.agentmain("rm bing.com")

        DnsCacheManipulator.getDnsCache("bing.com").shouldBeNull()
    }

    @Test
    fun test_agentmain_rm_withFile() {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3")
        DnsCacheManipulator.getDnsCache("bing.com").shouldNotBeNull()

        DcmAgent.agentmain("rm  bing.com file $outputFilePath")

        DnsCacheManipulator.getDnsCache("bing.com").shouldBeNull()
    }

    @Test
    fun test_agentmain_list() {
        DcmAgent.agentmain("   list  ")
        DcmAgent.agentmain("   ls  ")
    }

    @Test
    fun test_agentmain_clear() {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3")

        DcmAgent.agentmain("   clear  ")

        DnsCacheManipulator.listDnsCache().shouldBeEmpty()
    }

    @Test
    fun test_agentmain_setPolicy() {
        DcmAgent.agentmain("   setPolicy    345  ")

        DnsCacheManipulator.getDnsCachePolicy() shouldBe 345
    }

    @Test
    fun test_agentmain_getPolicy() {
        DnsCacheManipulator.setDnsCachePolicy(456)

        DcmAgent.agentmain("   getPolicy     ")

        DnsCacheManipulator.getDnsCachePolicy() shouldBe 456
    }

    @Test
    fun test_agentmain_setNegativePolicy() {
        DcmAgent.agentmain("   setNegativePolicy 42 ")

        DnsCacheManipulator.getDnsNegativeCachePolicy() shouldBe 42
    }

    @Test
    fun test_agentmain_getNegativePolicy() {
        DnsCacheManipulator.setDnsNegativeCachePolicy(45)

        DcmAgent.agentmain("   getNegativePolicy")

        DnsCacheManipulator.getDnsNegativeCachePolicy() shouldBe 45
    }

    @Test
    fun test_agentmain_skipNoActionArguments() {
        DcmAgent.agentmain("  arg1  arg2   ")
    }

    @Test
    fun test_agentmain_actionNeedMoreArgument() {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1110)

        DcmAgent.agentmain("  setNegativePolicy     file $outputFilePath")

        DnsCacheManipulator.getDnsNegativeCachePolicy() shouldBe 1110

        val content = FileUtils.readLines(outputFile, UTF8)
        content.first() shouldContain "Error to do action setNegativePolicy"
        content.first() shouldContain "Action setNegativePolicy need more argument!"
    }

    @Test
    fun test_agentmain_actionTooMoreArgument() {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1111)

        DcmAgent.agentmain("  setNegativePolicy 737 HaHa  file $outputFilePath")

        DnsCacheManipulator.getDnsNegativeCachePolicy() shouldBe 1111

        val content = FileUtils.readLines(outputFile, UTF8)
        content.first() shouldContain "Error to do action setNegativePolicy 737 HaHa"
        content.first() shouldContain "Too many arguments for action setNegativePolicy! arguments: [737, HaHa]"
    }

    @Test
    fun test_agentmain_unknownAction() {
        DcmAgent.agentmain("  unknownAction  arg1  arg2   file $outputFilePath")

        val content = FileUtils.readLines(outputFile, UTF8)
        content.first() shouldContain "No action in agent argument, do nothing!"
    }
}
