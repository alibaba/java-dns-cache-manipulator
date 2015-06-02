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
            System.out.printf("\tbaidu.com: %s\n\n",
                    InetAddress.getByName("baidu.com").getHostAddress());
            Thread.sleep(1000);
        }
    }
}
