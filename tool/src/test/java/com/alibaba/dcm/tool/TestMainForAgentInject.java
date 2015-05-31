package com.alibaba.dcm.tool;

import java.net.InetAddress;
import java.util.Date;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TestMainForAgentInject {
    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println(new Date());

            System.out.println("\tClass path:");
            final String classPath = System.getProperty("java.class.path");
            final String[] split = classPath.split(":");
            for (int i = 0; i < split.length; i++) {
                if (split[i].trim().isEmpty()) continue;
                System.out.printf("\t%6s %s\n", i + 1, split[i]);
            }

            System.out.printf("\tbaidu.com: %s\n", InetAddress.getByName("baidu.com").getHostAddress());
            System.out.println();

            Thread.sleep(1000);
        }
    }
}
