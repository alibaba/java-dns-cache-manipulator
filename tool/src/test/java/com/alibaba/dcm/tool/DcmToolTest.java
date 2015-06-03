package com.alibaba.dcm.tool;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author oldratlee
 */
public class DcmToolTest {
    File outputFile;
    String outputFilePath;

    String agentFilePath;

    @Before
    public void setUp() throws Exception {
        outputFile = new File("target/output.log");
        FileUtils.deleteQuietly(outputFile);
        FileUtils.touch(outputFile);
        assertTrue(outputFile.length() == 0);
        System.out.println("Prepared output file: " + outputFile.getAbsolutePath());

        outputFilePath = outputFile.getAbsolutePath();


        File dcmLibProjectDir = new File("../library");
        if (!dcmLibProjectDir.exists()) {
            dcmLibProjectDir = new File("library");

            if (!dcmLibProjectDir.exists()) {
                fail("Not found dcm lib project!");
            }
        }
        File dcmLibProjectTargetDir = new File(dcmLibProjectDir, "target");
        if (!dcmLibProjectTargetDir.exists()) {
            fail("Not found dcm lib project target dir!");
        }
        System.out.println("Found target dir: " + dcmLibProjectTargetDir);

        final Iterator<File> fileIterator = FileUtils.iterateFiles(dcmLibProjectTargetDir, new String[]{"jar"}, false);
        while (fileIterator.hasNext()) {
            final File next = fileIterator.next();
            final String fileName = next.getName();
            final String absolutePath = next.getAbsolutePath();

            if (fileName.startsWith("dns-cache-manipulator")) {
                final String replaced = fileName.replace("dns-cache-manipulator-", "").replace("-SNAPSHOT", "");
                if (!replaced.contains("-"))
                    agentFilePath = absolutePath;
            }
        }

        assertNotNull(agentFilePath);
        System.out.println("Found agent file: " + agentFilePath);
    }

    @Test
    public void test_main() throws Exception {
        System.setProperty(DcmTool.DCM_TOOLS_TMP_FILE, outputFilePath);
        System.setProperty(DcmTool.DCM_TOOLS_AGENT_JAR, agentFilePath);

        DcmTool.main(new String[]{"-p", pid(), "getPolicy"});
    }

    static String pid() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        final int idx = name.indexOf("@");
        return name.substring(0, idx);
    }
}
