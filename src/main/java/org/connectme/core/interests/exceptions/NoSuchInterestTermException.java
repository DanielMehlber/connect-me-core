package org.connectme.core.interests.exceptions;

/**
 * This exception is thrown if a certain interest term with an id has not been found or does not exist.
 * @author Daniel Mehlber
 */
public class NoSuchInterestTermException extends Exception {

    public NoSuchInterestTermException(Long id) {
        super(String.format("no such interest term with id:%d found", id));
    }

}
