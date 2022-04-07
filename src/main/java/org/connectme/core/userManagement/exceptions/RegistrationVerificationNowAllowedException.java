package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown if another verification is currently not allowed in the verification process.
 */
public class RegistrationVerificationNowAllowedException extends Exception {

    public RegistrationVerificationNowAllowedException() {
        super("Verification not currently allowed. User should try again later.");
    }

}
