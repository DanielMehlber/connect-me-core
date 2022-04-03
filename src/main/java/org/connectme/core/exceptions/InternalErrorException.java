package org.connectme.core.exceptions;

/**
 * This exception is thrown if a fatal internal error occurred.
 * Example: Database error
 */
public class InternalErrorException extends Exception {

    public InternalErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
