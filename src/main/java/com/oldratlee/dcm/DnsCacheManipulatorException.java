package com.oldratlee.dcm;

/**
 * @author ding.lid
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
