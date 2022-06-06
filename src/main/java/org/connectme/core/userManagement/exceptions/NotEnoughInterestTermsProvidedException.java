package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown, if there are not enough interest terms provided for the user profile. A user profile needs
 * a certain amount of interest terms associated with it.
 * @author Daniel Mehlber
 */
public class NotEnoughInterestTermsProvidedException extends Exception {

    public NotEnoughInterestTermsProvidedException(int amountProvided, int amountNeeded) {
        super(String.format("a user profile needs at least %d interest terms associated with it, only %d where provided", amountNeeded, amountProvided));
    }

}
