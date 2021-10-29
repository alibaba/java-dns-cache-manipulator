package com.alibaba.dcm.tool;

import java.net.InetAddress;
import java.util.Date;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class MainForAgentInject {
    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    public static void main(String[] args) throws Exception {
        final String host = "bing.com";
        while (true) {
            System.out.printf("%s: %s %s\n",
                    new Date(),
                    host,
                    InetAddress.getByName(host).getHostAddress()
            );

            Thread.sleep(1000);
        }
    }
}
