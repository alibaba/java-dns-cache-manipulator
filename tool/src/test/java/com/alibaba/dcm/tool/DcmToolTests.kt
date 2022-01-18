package com.alibaba.dcm.tool

import io.kotest.assertions.withClue
import io.kotest.core.annotation.EnabledCondition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.net.InetAddress
import kotlin.reflect.KClass
import kotlin.streams.toList

/**
 * https://kotest.io/docs/framework/testing-styles.html#annotation-spec
 */
// Ignore "attach to current VM" test for jdk 9+, since java 9+ does not support
//     "java.io.IOException: Can not attach to current VM"
@EnabledIf(Java8Only::class)
class DcmToolTests : AnnotationSpec() {

    private lateinit var agentFilePath: String

    @BeforeAll
    fun prepareAgentFilePath() {
        val agentFile = findAgentFileFromLibProject() ?: findAgentFileFromMavenLocal()
        withClue("Not found agent file") {
            agentFile.shouldNotBeNull()
        }

        agentFilePath = agentFile!!
        println("Found agent file: $agentFilePath")
    }

    @BeforeEach
    fun setUp() {
        val outputFile = File.createTempFile("dcm-output-", ".log")
        FileUtils.deleteQuietly(outputFile)
        FileUtils.touch(outputFile)
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
        val dcmLibProjectDir = listOf("library", "../library", "../../library")
            .asSequence()
            .map { File(it) }
            .filter { it.exists() }
            .firstOrNull()
            ?: return null

        val targetDir = File(dcmLibProjectDir, "target")
        if (!targetDir.exists()) return null
        println("Found target dir: ${targetDir.canonicalPath}")

        return FileUtils.streamFiles(targetDir, false, "jar")
            .filter { isAgentJar(it) }
            .findFirst()
            .map { it.canonicalPath }
            .orElse(null)
    }

    private fun findAgentFileFromMavenLocal(): String? {
        val home = System.getProperty("user.home")
        val m2DcmLibDependencyDir = File("$home/.m2/repository/com/alibaba/dns-cache-manipulator")

        return FileUtils.streamFiles(m2DcmLibDependencyDir, true, "jar")
            .filter { isAgentJar(it) }
            .map { it.canonicalPath }
            .toList()
            .maxOrNull()
    }

    private fun isAgentJar(file: File): Boolean {
        val fileName = file.name
        if (!fileName.startsWith("dns-cache-manipulator")) return false

        val replaced = fileName.replace("dns-cache-manipulator-", "").replace("-SNAPSHOT", "")
        return !replaced.contains("-")
    }
}

/**
 * https://kotest.io/docs/framework/conditional/spec-annotations-conditional-evaluation.html
 */
class Java8Only : EnabledCondition {
    override fun enabled(kclass: KClass<out Spec>): Boolean = SystemUtils.IS_JAVA_1_8
}
