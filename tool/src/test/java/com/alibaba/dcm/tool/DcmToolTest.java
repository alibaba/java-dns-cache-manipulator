package com.alibaba.dcm.tool;

import com.alibaba.support.junit.conditional.AboveJava8;
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmToolTest {
    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    File outputFile;
    String outputFilePath;

    String agentFilePath;

    @Before
    public void setUp() throws Exception {
        outputFile = new File("target/output.log");
        FileUtils.deleteQuietly(outputFile);
        FileUtils.touch(outputFile);
        assertEquals(0, outputFile.length());

        outputFilePath = outputFile.getCanonicalPath();
        System.out.println("Prepared output file: " + outputFilePath);

        setAgentFilePath();
        assertNotNull(agentFilePath);
        System.out.println("Found agent file: " + agentFilePath);
    }

    public void setAgentFilePath() throws Exception {
        // find agent jar file from dcm lib project
        File dcmLibProjectDir = new File("library");
        if (!dcmLibProjectDir.exists()) {
            dcmLibProjectDir = new File("../library");
        }
        if (!dcmLibProjectDir.exists()) {
            dcmLibProjectDir = new File("../../library");
        }
        if (dcmLibProjectDir.exists()) {
            File dcmLibProjectTargetDir = new File(dcmLibProjectDir, "target");
            if (dcmLibProjectTargetDir.exists()) {
                System.out.println("Found target dir: " + dcmLibProjectTargetDir + " " + dcmLibProjectDir.getCanonicalPath());
            }

            final Iterator<File> fileIterator = FileUtils.iterateFiles(dcmLibProjectTargetDir, new String[]{"jar"}, false);
            while (fileIterator.hasNext()) {
                final File next = fileIterator.next();
                final String fileName = next.getName();
                final String agentJarPath = next.getCanonicalPath();
                System.out.println("List Agent jar from target: " + agentJarPath);

                if (fileName.startsWith("dns-cache-manipulator")) {
                    final String replaced = fileName.replace("dns-cache-manipulator-", "").replace("-SNAPSHOT", "");
                    if (!replaced.contains("-")) {
                        agentFilePath = agentJarPath;
                    }
                }
            }
        }

        // find agent jar file from maven local repository
        if (agentFilePath == null) {
            String home = System.getProperty("user.home");
            String m2DcmLibDependencyDir = home + "/.m2/repository/com/alibaba/dns-cache-manipulator";

            List<String> list = new ArrayList<String>();
            final Iterator<File> fileIterator = FileUtils.iterateFiles(new File(m2DcmLibDependencyDir), new String[]{"jar"}, true);
            while (fileIterator.hasNext()) {
                final File next = fileIterator.next();
                final String fileName = next.getName();
                final String agentJarPath = next.getCanonicalPath();

                if (fileName.startsWith("dns-cache-manipulator")) {
                    final String replaced = fileName.replace("dns-cache-manipulator-", "").replace("-SNAPSHOT", "");
                    if (!replaced.contains("-")) {
                        list.add(agentJarPath);
                    }
                }
            }

            assertTrue(list.size() > 0);
            Collections.sort(list);

            System.out.println("List Agent jar from .m2: " + list);
            agentFilePath = list.get(list.size() - 1);
        }
    }

    @Test
    // Ignore "attach to current VM" test for jdk 9+, since java 9+ does not support
    //   "java.io.IOException: Can not attach to current VM"
    @ConditionalIgnoreRule.ConditionalIgnore(condition = AboveJava8.class)
    public void test_main_getPolicy() throws Exception {
        System.setProperty(DcmTool.DCM_TOOLS_TMP_FILE_KEY, outputFilePath);
        System.setProperty(DcmTool.DCM_TOOLS_AGENT_JAR_KEY, agentFilePath);

        DcmTool.main(new String[]{"-p", DcmTool.pid(), "getPolicy"});
    }

    @Test
    // Ignore "attach to current VM" test for jdk 9+, since java 9+ does not support
    //   "java.io.IOException: Can not attach to current VM"
    @ConditionalIgnoreRule.ConditionalIgnore(condition = AboveJava8.class)
    public void test_main_set() throws Exception {
        System.setProperty(DcmTool.DCM_TOOLS_TMP_FILE_KEY, outputFilePath);
        System.setProperty(DcmTool.DCM_TOOLS_AGENT_JAR_KEY, agentFilePath);

        final String ip = "1.1.2.2";
        final String host = "bing.com";

        DcmTool.main(new String[]{"-p", DcmTool.pid(), "set", host, ip});
        assertEquals(ip, InetAddress.getByName(host).getHostAddress());
    }
}
