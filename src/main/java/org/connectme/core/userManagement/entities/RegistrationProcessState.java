package org.connectme.core.userManagement.entities;

/**
 * This enum contains all states the Registration object goes through
 */
public enum RegistrationProcessState {

    /**
     * When created, nothing is set
     */
    CREATED,

    /**
     * In Phase 1 the user passes username and password
     */
    USERNAME_PASSWORD_SET,

    /**
     * In Phase 2 the user passes his phone number (that is not verified yet)
     */
    PHONE_NUMBER_SET,

    /**
     * In Phase 3 the user must verify his phone number (with generated verification code)
     */
    WAITING_FOR_PHONE_NUMBER_VERIFICATION,


    /**
     * In Phase 3 the user has verified his phone number
     */
    PHONE_NUMBER_VERIFIED
}
