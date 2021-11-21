package com.alibaba.dcm.agent;

import com.alibaba.dcm.DnsCacheManipulator;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmAgentTest {
    private static final String UTF8 = "UTF-8";

    private File outputFile;
    private String outputFilePath;

    @Before
    public void setUp() throws Exception {
        outputFile = File.createTempFile("dcm-output-", ".log");
        FileUtils.deleteQuietly(outputFile);
        FileUtils.touch(outputFile);
        assertEquals(0, outputFile.length());
        System.out.println("Prepared output file: " + outputFile.getAbsolutePath());

        outputFilePath = outputFile.getAbsolutePath();

        DnsCacheManipulator.clearDnsCache();
    }

    @Test
    public void test_agentmain_empty() throws Exception {
        DcmAgent.agentmain("   ");
    }

    @Test
    public void test_agentmain_file() throws Exception {
        DcmAgent.agentmain("file " + outputFilePath);

        final List<String> content = FileUtils.readLines(outputFile, UTF8);
        assertThat(content.get(0), containsString("No action in agent argument, do nothing!"));
    }

    @Test
    public void test_agentmain_set() throws Exception {
        DcmAgent.agentmain("set bing.com 1.2.3.4");
        assertEquals("1.2.3.4", DnsCacheManipulator.getDnsCache("bing.com").getIp());
    }

    @Test
    public void test_agentmain_set_toFile() throws Exception {
        DcmAgent.agentmain("set bing.com 1.2.3.4 file " + outputFilePath);
        assertEquals("1.2.3.4", DnsCacheManipulator.getDnsCache("bing.com").getIp());

        final List<String> content = FileUtils.readLines(outputFile, UTF8);
        assertEquals(DcmAgent.DCM_AGENT_SUCCESS_MARK_LINE, content.get(content.size() - 1));
    }

    @Test
    public void test_agentmain_set_MultiIp() throws Exception {
        DcmAgent.agentmain("set bing.com 1.1.1.1 2.2.2.2");
        assertArrayEquals(new String[]{"1.1.1.1", "2.2.2.2"}, DnsCacheManipulator.getDnsCache("bing.com").getIps());
    }

    @Test
    public void test_agentmain_get() throws Exception {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3");
        DcmAgent.agentmain("get bing.com");
    }

    @Test
    public void test_agentmain_rm() throws Exception {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3");
        DcmAgent.agentmain("rm bing.com");

        assertNull(DnsCacheManipulator.getDnsCache("bing.com"));
    }

    @Test
    public void test_agentmain_rm_withFile() throws Exception {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3");
        assertNotNull(DnsCacheManipulator.getDnsCache("bing.com"));
        DcmAgent.agentmain("rm  bing.com file " + outputFilePath);

        assertNull(DnsCacheManipulator.getDnsCache("bing.com"));
    }

    @Test
    public void test_agentmain_list() throws Exception {
        DcmAgent.agentmain("   list  ");
        DcmAgent.agentmain("   ls  ");
    }

    @Test
    public void test_agentmain_clear() throws Exception {
        DnsCacheManipulator.setDnsCache("bing.com", "3.3.3.3");
        DcmAgent.agentmain("   clear  ");
        assertEquals(0, DnsCacheManipulator.listDnsCache().size());
    }

    @Test
    public void test_agentmain_setPolicy() throws Exception {
        DcmAgent.agentmain("   setPolicy    345  ");
        assertEquals(345, DnsCacheManipulator.getDnsCachePolicy());
    }

    @Test
    public void test_agentmain_getPolicy() throws Exception {
        DnsCacheManipulator.setDnsCachePolicy(456);
        DcmAgent.agentmain("   getPolicy     ");
        assertEquals(456, DnsCacheManipulator.getDnsCachePolicy());
    }

    @Test
    public void test_agentmain_setNegativePolicy() throws Exception {
        DcmAgent.agentmain("   setNegativePolicy 42 ");
        assertEquals(42, DnsCacheManipulator.getDnsNegativeCachePolicy());
    }

    @Test
    public void test_agentmain_getNegativePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(45);
        DcmAgent.agentmain("   getNegativePolicy");
        assertEquals(45, DnsCacheManipulator.getDnsNegativeCachePolicy());
    }

    @Test
    public void test_agentmain_skipNoActionArguments() throws Exception {
        DcmAgent.agentmain("  arg1  arg2   ");
    }

    @Test
    public void test_agentmain_actionNeedMoreArgument() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1110);

        DcmAgent.agentmain("  setNegativePolicy     file " + outputFilePath);

        assertEquals(1110, DnsCacheManipulator.getDnsNegativeCachePolicy());

        final List<String> content = FileUtils.readLines(outputFile, UTF8);
        assertThat(content.get(0), containsString("Error to do action setNegativePolicy"));
        assertThat(content.get(0), containsString("Action setNegativePolicy need more argument!"));
    }

    @Test
    public void test_agentmain_actionTooMoreArgument() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(1111);

        DcmAgent.agentmain("  setNegativePolicy 737 HaHa  file " + outputFilePath);

        assertEquals(1111, DnsCacheManipulator.getDnsNegativeCachePolicy());

        final List<String> content = FileUtils.readLines(outputFile, UTF8);
        assertThat(content.get(0), containsString("Error to do action setNegativePolicy 737 HaHa"));
        assertThat(content.get(0), containsString("Too many arguments for action setNegativePolicy! arguments: [737, HaHa]"));
    }

    @Test
    public void test_agentmain_unknownAction() throws Exception {
        DcmAgent.agentmain("  unknownAction  arg1  arg2   file " + outputFilePath);

        final List<String> content = FileUtils.readLines(outputFile, UTF8);
        assertThat(content.get(0), containsString("No action in agent argument, do nothing!"));
    }
}
