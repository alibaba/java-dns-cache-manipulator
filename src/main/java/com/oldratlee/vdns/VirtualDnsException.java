package com.oldratlee.vdns;

/**
 * @author ding.lid
 */
public class VirtualDnsException extends RuntimeException {
    private static final long serialVersionUID = -7843069964883320844L;

    public VirtualDnsException(String message) {
        super(message);
    }

    public VirtualDnsException(String message, Throwable cause) {
        super(message, cause);
    }
}
