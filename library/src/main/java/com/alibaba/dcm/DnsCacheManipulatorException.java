package com.alibaba.dcm;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DnsCacheManipulatorException extends RuntimeException {
    private static final long serialVersionUID = -7843069964883320844L;

    public DnsCacheManipulatorException(String message) {
        super(message);
    }

    public DnsCacheManipulatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
