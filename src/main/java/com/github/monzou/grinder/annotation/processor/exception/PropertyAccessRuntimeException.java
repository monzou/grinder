package com.github.monzou.grinder.annotation.processor.exception;

/**
 * PropertyAccessRuntimeException
 */
@SuppressWarnings("serial")
public class PropertyAccessRuntimeException extends RuntimeException {

    public PropertyAccessRuntimeException(String message) {
        super(message);
    }

}
