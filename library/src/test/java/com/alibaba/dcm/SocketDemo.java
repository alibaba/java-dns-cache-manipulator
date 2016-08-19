package com.alibaba.dcm;

import org.apache.commons.io.IOUtils;

import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class SocketDemo {
    private static final Charset encoding = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        new Socket("www.qq.com", 80);
        Socket client = new Socket("www.baidu.com", 80);

        client.setSoTimeout(30 * 1000);

        IOUtils.write("Hello world!", client.getOutputStream(), encoding);

        final String input = IOUtils.toString(client.getInputStream(),encoding);
        System.out.println(input);
        System.out.println("bye!");

        client.close();
    }
}
