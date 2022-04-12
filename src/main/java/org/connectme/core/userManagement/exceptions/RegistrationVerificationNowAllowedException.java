package org.connectme.core.userManagement.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown if another verification is currently not allowed in the verification process.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class RegistrationVerificationNowAllowedException extends Exception {

    public RegistrationVerificationNowAllowedException() {
        super("Verification not currently allowed. User should try again later.");
    }

}
