package org.connectme.core.userManagement.exceptions;

/**
 * This exception is thrown if the user passed the wrong verification code
 */
public class WrongVerificationCodeException extends Exception {

    private final String wrongVerificationCode;

    public WrongVerificationCodeException(final String wrongVerificationCode) {
        /*
         * SECURITY: Do NOT log raw user input, it could be harmful (see Log-Injection)
         */
        super("user passed wrong verification code");
        this.wrongVerificationCode = wrongVerificationCode;
    }

    public String getWrongVerificationCode() {
        return wrongVerificationCode;
    }
}
