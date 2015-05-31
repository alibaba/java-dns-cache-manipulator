package com.alibaba.dcm.agent;

import com.alibaba.dcm.DnsCacheManipulator;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DcmAgentTest {
    File outputFile;

    @Before
    public void setUp() throws Exception {
        outputFile = new File("target/output.log");
        FileUtils.deleteQuietly(outputFile);
        FileUtils.touch(outputFile);
        assertTrue(outputFile.length() == 0);
        System.out.println("Prepared output file: " + outputFile.getAbsolutePath());

        DnsCacheManipulator.clearDnsCache();
    }

    @Test
    public void test_agentmain_empty() throws Exception {
        DcmAgent.agentmain("   ");
    }

    @Test
    public void test_agentmain_file() throws Exception {
        final String output = outputFile.getAbsolutePath();
        DcmAgent.agentmain("file " + output);

        final String text = FileUtils.readFileToString(new File(output));
        assertTrue(text.length() > 0);
        System.out.println(text);
    }

    @Test
    public void test_agentmain_set() throws Exception {
        DcmAgent.agentmain("set baidu.com 1.2.3.4");
        assertEquals("1.2.3.4", DnsCacheManipulator.getDnsCache("baidu.com").getIp());
    }

    @Test
    public void test_agentmain_set_MultiIp() throws Exception {
        DcmAgent.agentmain("set baidu.com 1.1.1.1 2.2.2.2");
        assertArrayEquals(new String[]{"1.1.1.1", "2.2.2.2"}, DnsCacheManipulator.getDnsCache("baidu.com").getIps());
    }

    @Test
    public void test_agentmain_get() throws Exception {
        DnsCacheManipulator.setDnsCache("baidu.com", "3.3.3.3");
        DcmAgent.agentmain("get baidu.com");
    }

    @Test
    public void test_agentmain_list() throws Exception {
        DcmAgent.agentmain("   list  ");
    }

    @Test
    public void test_agentmain_clear() throws Exception {
        DnsCacheManipulator.setDnsCache("baidu.com", "3.3.3.3");
        DcmAgent.agentmain("   clear  ");
        assertEquals(0, DnsCacheManipulator.listDnsCache().size());
    }

    @Test
    public void test_agentmain_setPolicy() throws Exception {
        DcmAgent.agentmain("   setPolicy    5  ");
        assertEquals(5, DnsCacheManipulator.getDnsCachePolicy());
    }

    @Test
    public void test_agentmain_getPolicy() throws Exception {
        DnsCacheManipulator.setDnsCachePolicy(77);
        DcmAgent.agentmain("   getPolicy    5  ");
        assertEquals(77, DnsCacheManipulator.getDnsCachePolicy());
    }

    @Test
    public void test_agentmain_setNegativePolicy() throws Exception {
        DcmAgent.agentmain("   setNegativePolicy    42  ");
        assertEquals(42, DnsCacheManipulator.getDnsNegativeCachePolicy());
    }

    @Test
    public void test_agentmain_getNegativePolicy() throws Exception {
        DnsCacheManipulator.setDnsNegativeCachePolicy(45);
        DcmAgent.agentmain("   getNegativePolicy    5  ");
        assertEquals(45, DnsCacheManipulator.getDnsNegativeCachePolicy());
    }
    
    @Test
    public void test_agentmain_skipNoActionArguments() throws Exception {
        DcmAgent.agentmain("  arg1  arg2   ");
    }
    
    @Test
    public void test_agentmain_actionNeedMoreArgument() throws Exception {
        DcmAgent.agentmain("  setNegativePolicy   ");
    }
    
    @Test
    public void test_agentmain_actionTooMoreArgument() throws Exception {
        DcmAgent.agentmain("  setNegativePolicy 737 HaHa  ");
    }

    @Test
    public void test_agentmain_unknownAction() throws Exception {
        DcmAgent.agentmain("  unknownAction  arg1  arg2   ");
    }
}
