package org.connectme.core.exceptions;

/**
 * This exception is thrown if the user passed the wrong verification code
 */
public class WrongVerificationCodeException extends Exception {

    private final String wrongVerificationCode;

    public WrongVerificationCodeException(final String wrongVerificationCode) {
        super(String.format("user passed wrong verification code '%s'", wrongVerificationCode));
        this.wrongVerificationCode = wrongVerificationCode;
    }

    public String getWrongVerificationCode() {
        return wrongVerificationCode;
    }
}
