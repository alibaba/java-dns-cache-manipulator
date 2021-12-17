package com.alibaba.dcm;

/**
 * Thrown to indicate that the operation of {@link DnsCacheManipulator} failed.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see DnsCacheManipulator
 */
public final class DnsCacheManipulatorException extends RuntimeException {
    private static final long serialVersionUID = -7843069964883320844L;

    /**
     * Constructs a {@link DnsCacheManipulatorException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public DnsCacheManipulatorException(String message) {
        super(message);
    }

    /**
     * Constructs a {@link DnsCacheManipulatorException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method).
     */
    public DnsCacheManipulatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
