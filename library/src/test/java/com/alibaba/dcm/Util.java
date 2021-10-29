package com.alibaba.dcm;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Util {
    public static String getIpByName(String name) throws UnknownHostException {
        return InetAddress.getByName(name).getHostAddress();
    }
}
