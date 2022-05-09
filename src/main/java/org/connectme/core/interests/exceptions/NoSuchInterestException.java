package org.connectme.core.interests.exceptions;

/**
 * This exception is thrown if an interest cannot be found
 */
public class NoSuchInterestException extends Exception {

    public NoSuchInterestException(final Long id) {
        super(String.format("interest with id %d does not exist", id));
    }

}
