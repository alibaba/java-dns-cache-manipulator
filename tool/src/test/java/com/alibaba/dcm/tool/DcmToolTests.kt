package com.alibaba.dcm.tool

import io.kotest.assertions.fail
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.SystemUtils
import org.apache.maven.artifact.versioning.ComparableVersion
import java.io.File
import java.net.InetAddress

/**
 * https://kotest.io/docs/framework/testing-styles.html#annotation-spec
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DcmToolTests : AnnotationSpec() {

    // Ignore "attach to current VM" test for jdk 9+, since java 9+ does not support
    //     "java.io.IOException: Can not attach to current VM"
    @Suppress("OverridingDeprecatedMember")
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = SystemUtils.IS_JAVA_1_8)

    private lateinit var agentFilePath: String

    @BeforeAll
    fun prepareAgentFilePath() {
        println("work dir: ${File("").absolutePath}")

        agentFilePath = findAgentFileFromLibProject() ?: findAgentFileFromMavenLocal()
                ?: fail("Not found agent file")

        println("Found agent file: $agentFilePath")
    }

    @BeforeEach
    fun setUp() {
        val outputFile = tempfile("dcm-output-", ".log")
        outputFile.length() shouldBe 0

        val outputFilePath = outputFile.canonicalPath
        println("Prepared output file: $outputFilePath")

        System.setProperty(DcmTool.DCM_TOOLS_TMP_FILE_KEY, outputFilePath)
        System.setProperty(DcmTool.DCM_TOOLS_AGENT_JAR_KEY, agentFilePath)
    }

    @Test
    fun test_main_getPolicy() {
        DcmTool.main(arrayOf("-p", DcmTool.pid(), "getPolicy"))
    }

    @Test
    fun test_main_set() {
        val ip = "1.1.2.2"
        val host = "bing.com"

        DcmTool.main(arrayOf("-p", DcmTool.pid(), "set", host, ip))
        InetAddress.getByName(host).hostAddress shouldBe ip
    }

    private fun findAgentFileFromLibProject(): String? {
        val dcmLibProjectDir: File = listOf("library", "../library", "../../library")
            .asSequence()
            .map { File(it) }
            .filter { it.exists() }
            .firstOrNull()
            ?: return null

        val targetDir = File(dcmLibProjectDir, "target")
        if (!targetDir.exists()) return null
        println("Found target dir: ${targetDir.canonicalPath}")

        return targetDir.walk()
            .filter { it.extension == "jar" && isAgentJar(it) }
            .map { it.canonicalPath }
            .firstOrNull()
    }

    private fun findAgentFileFromMavenLocal(): String? {
        val home: String = System.getProperty("user.home")
        val m2DcmLibDependencyDir = File("$home/.m2/repository/com/alibaba/dns-cache-manipulator")

        return m2DcmLibDependencyDir.walk()
            .filter { it.extension == "jar" && isAgentJar(it) }
            .map { it.canonicalPath }
            .maxWithOrNull(Comparator.comparing { ComparableVersion(it.trimToVersion()) })
    }

    private fun isAgentJar(file: File): Boolean {
        val fileName: String = file.name
        if (!fileName.startsWith("dns-cache-manipulator-")) return false

        return !fileName.trimToVersion().removeSuffix("-SNAPSHOT").contains("-")
    }

    private fun String.trimToVersion() = substringAfterLast('/')
        .removePrefix("dns-cache-manipulator-").removeSuffix(".jar")
}
