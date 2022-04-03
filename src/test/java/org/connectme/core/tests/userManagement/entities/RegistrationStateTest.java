package org.connectme.core.tests.userManagement.entities;

import org.connectme.core.exceptions.RegistrationVerificationNowAllowedException;
import org.connectme.core.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.entities.Registration;
import org.connectme.core.userManagement.entities.RegistrationState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class RegistrationStateTest {

    @Test
    public void happyPath() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException {
        /*
         * SCENARIO: go through happy path of registration process
         */

        String username = "username";
        String password = "password";
        String phoneNumber = "0 000 000000";

        Registration registration = new Registration();

        registration.setUsernameAndPassword(username, password);
        Assertions.assertEquals(registration.getUsername(), username);
        Assertions.assertEquals(registration.getPassword(), password);

        registration.setPhoneNumber(phoneNumber);
        Assertions.assertEquals(registration.getPhoneNumber(), phoneNumber);

        registration.startAndWaitForVerification();

        String code = registration.getVerificationCode();
        registration.checkVerificationCode(code);

        Assertions.assertTrue(registration.isVerified());
        Assertions.assertSame(registration.getState(), RegistrationState.PHONE_NUMBER_VERIFIED);
    }

    @Test
    public void exceedVerificationLimit() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException {
        /*
         * SCENARIO: evil or clumsy user enters wrong verification code too often and has to wait for a certain amount
         * of time. The amount of verification attempts per time has to be limited because SMS costs money.
         *
         * Test this security mechanism
         */

        Registration registration = new Registration();

        registration.setUsernameAndPassword("username", "password");
        registration.setPhoneNumber("0 0000 000000");

        // exceed max amount of allowed verifications attempts
        for (int i = 0; i <= Registration.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            registration.startAndWaitForVerification();
            try {
                registration.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, registration::startAndWaitForVerification);

        // reduce time to wait in order to complete unit test faster
        registration.setLastVerificationAttempt(LocalDateTime.now().minusMinutes(Registration.BLOCK_FAILED_ATTEMPT_MINUTES));

        // try again (this time with the correct code)
        registration.startAndWaitForVerification();
        String code = registration.getVerificationCode();
        registration.checkVerificationCode(code);
    }

    @Test
    public void attemptProcessRestartWhileVerificationBlock() throws RegistrationVerificationNowAllowedException {
        /*
         * SCENARIO: evil user tries to send infinite verification SMS in order to harm us:
         * After he attempted too many verifications he must wait. To bypass that, he tries to reset the
         * registration process. This action is not allowed while the verification is "blocked".
         *
         * Test this security mechanism
         */

        Registration registration = new Registration();

        registration.setUsernameAndPassword("username", "password");
        registration.setPhoneNumber("0 0000 000000");

        // exceed max attempt of verifications
        for (int i = 0; i <= Registration.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            registration.startAndWaitForVerification();
            try {
                registration.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, registration::startAndWaitForVerification);

        // try to reset registration in order to illegally bypass blocked time. Must be interrupted by exception.
        Assertions.assertThrows(IllegalStateException.class, registration::reset);
    }

    @Test
    public void attemptIllegalInteractionsToStates() throws WrongVerificationCodeException, RegistrationVerificationNowAllowedException {
        /*
         * SCENARIO: In every state of the registration only certain interactions are allowed.
         *
         * Test that other interactions are not allowed
         */

        // Set state to CREATED, following interactions are not allowed:
        Registration registration = new Registration();
        Assertions.assertThrows(IllegalStateException.class, registration::startAndWaitForVerification);
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setPhoneNumber(""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.checkVerificationCode(""));

        // Set state to USERNAME_PASSWORD_SET, following interactions are not allowed:
        registration.setUsernameAndPassword("username", "password");
        Assertions.assertThrows(IllegalStateException.class, registration::startAndWaitForVerification);
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setUsernameAndPassword("", ""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.checkVerificationCode(""));

        // Set state to PHONE_NUMBER_SET, following interactions are not allowed:
        registration.setPhoneNumber("");
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setUsernameAndPassword("", ""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setPhoneNumber(""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.checkVerificationCode(""));

        // Set state to WAITING_FOR_PHONE_NUMBER_VERIFICATION, following interactions are not allowed:
        registration.startAndWaitForVerification();
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setUsernameAndPassword("", ""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setPhoneNumber(""));
        Assertions.assertThrows(IllegalStateException.class, registration::startAndWaitForVerification);

        // Set state to PHONE_NUMBER_VERIFIED, following interactions are not allowed:
        registration.checkVerificationCode(registration.getVerificationCode());
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setUsernameAndPassword("", ""));
        Assertions.assertThrows(IllegalStateException.class, () -> registration.setPhoneNumber(""));
        Assertions.assertThrows(IllegalStateException.class, registration::startAndWaitForVerification);
    }

}
