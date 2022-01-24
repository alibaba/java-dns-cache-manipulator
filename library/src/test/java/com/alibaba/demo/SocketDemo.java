package com.alibaba.demo;

import org.apache.commons.io.IOUtils;

import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class SocketDemo {

    public static void main(String[] args) throws Exception {
        Socket client = new Socket("www.bing.com", 80);

        client.setSoTimeout(30 * 1000);

        IOUtils.write("Hello world!", client.getOutputStream(), UTF_8);

        final String input = IOUtils.toString(client.getInputStream(), UTF_8);
        System.out.println(input);
        System.out.println("bye!");

        client.close();
    }
}
