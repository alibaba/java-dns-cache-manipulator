package com.alibaba.dcm;

import org.apache.commons.io.IOUtils;

import java.net.Socket;

/**
 * @author ding.lid
 */
public class SocketDemo {
    public static void main(String[] args) throws Exception {
        new Socket("www.qq.com", 80);
        Socket client = new Socket("www.baidu.com", 80);

        client.setSoTimeout(30 * 1000);

        IOUtils.write("Hello world!", client.getOutputStream());

        final String input = IOUtils.toString(client.getInputStream());
        System.out.println(input);
        System.out.println("bye!");

        client.close();
    }
}
