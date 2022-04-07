package org.connectme.core.tests.userManagement.processes;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.exceptions.RegistrationVerificationNowAllowedException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.processes.RegistrationProcess;
import org.connectme.core.userManagement.processes.RegistrationProcessState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class RegistrationProcessStateTest {

    @Test
    public void happyPath() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException, ForbiddenInteractionException {
        /*
         * SCENARIO: go through happy path of registration process
         */

        String username = "username";
        String password = "password";
        String phoneNumber = "0 000 000000";

        RegistrationProcess registrationProcess = new RegistrationProcess();

        registrationProcess.setUsernameAndPassword(username, password);
        Assertions.assertEquals(registrationProcess.getUsername(), username);
        Assertions.assertEquals(registrationProcess.getPassword(), password);

        registrationProcess.setPhoneNumber(phoneNumber);
        Assertions.assertEquals(registrationProcess.getPhoneNumber(), phoneNumber);

        registrationProcess.startAndWaitForVerification();

        String code = registrationProcess.getVerificationCode();
        registrationProcess.checkVerificationCode(code);

        Assertions.assertTrue(registrationProcess.isVerified());
        Assertions.assertSame(registrationProcess.getState(), RegistrationProcessState.PHONE_NUMBER_VERIFIED);
    }

    @Test
    public void exceedVerificationLimit() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException, ForbiddenInteractionException {
        /*
         * SCENARIO: evil or clumsy user enters wrong verification code too often and has to wait for a certain amount
         * of time. The amount of verification attempts per time has to be limited because SMS costs money.
         *
         * Test this security mechanism
         */

        RegistrationProcess registrationProcess = new RegistrationProcess();

        registrationProcess.setUsernameAndPassword("username", "password");
        registrationProcess.setPhoneNumber("0 0000 000000");

        // exceed max amount of allowed verifications attempts
        for (int i = 0; i <= RegistrationProcess.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            registrationProcess.startAndWaitForVerification();
            try {
                registrationProcess.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, registrationProcess::startAndWaitForVerification);

        // reduce time to wait in order to complete unit test faster
        registrationProcess.setLastVerificationAttempt(LocalDateTime.now().minusMinutes(RegistrationProcess.BLOCK_FAILED_ATTEMPT_MINUTES));

        // try again (this time with the correct code)
        registrationProcess.startAndWaitForVerification();
        String code = registrationProcess.getVerificationCode();
        registrationProcess.checkVerificationCode(code);
    }

    @Test
    public void attemptProcessRestartWhileVerificationBlock() throws RegistrationVerificationNowAllowedException, ForbiddenInteractionException {
        /*
         * SCENARIO: evil user tries to send infinite verification SMS in order to harm us:
         * After he attempted too many verifications he must wait. To bypass that, he tries to reset the
         * registration process. This action is not allowed while the verification is "blocked".
         *
         * Test this security mechanism
         */

        RegistrationProcess registrationProcess = new RegistrationProcess();

        registrationProcess.setUsernameAndPassword("username", "password");
        registrationProcess.setPhoneNumber("0 0000 000000");

        // exceed max attempt of verifications
        for (int i = 0; i <= RegistrationProcess.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            registrationProcess.startAndWaitForVerification();
            try {
                registrationProcess.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, registrationProcess::startAndWaitForVerification);

        // try to reset registration in order to illegally bypass blocked time. Must be interrupted by exception.
        Assertions.assertThrows(ForbiddenInteractionException.class, registrationProcess::reset);
    }

    @Test
    public void attemptIllegalInteractionsToStates() throws WrongVerificationCodeException, RegistrationVerificationNowAllowedException, ForbiddenInteractionException {
        /*
         * SCENARIO: In every state of the registration only certain interactions are allowed.
         *
         * Test that other interactions are not allowed
         */

        // Set state to CREATED, following interactions are not allowed:
        RegistrationProcess registrationProcess = new RegistrationProcess();
        Assertions.assertThrows(ForbiddenInteractionException.class, registrationProcess::startAndWaitForVerification);
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setPhoneNumber(""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.checkVerificationCode(""));

        // Set state to USERNAME_PASSWORD_SET, following interactions are not allowed:
        registrationProcess.setUsernameAndPassword("username", "password");
        Assertions.assertThrows(ForbiddenInteractionException.class, registrationProcess::startAndWaitForVerification);
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setUsernameAndPassword("", ""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.checkVerificationCode(""));

        // Set state to PHONE_NUMBER_SET, following interactions are not allowed:
        registrationProcess.setPhoneNumber("");
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setUsernameAndPassword("", ""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setPhoneNumber(""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.checkVerificationCode(""));

        // Set state to WAITING_FOR_PHONE_NUMBER_VERIFICATION, following interactions are not allowed:
        registrationProcess.startAndWaitForVerification();
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setUsernameAndPassword("", ""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setPhoneNumber(""));
        Assertions.assertThrows(ForbiddenInteractionException.class, registrationProcess::startAndWaitForVerification);

        // Set state to PHONE_NUMBER_VERIFIED, following interactions are not allowed:
        registrationProcess.checkVerificationCode(registrationProcess.getVerificationCode());
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setUsernameAndPassword("", ""));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> registrationProcess.setPhoneNumber(""));
        Assertions.assertThrows(ForbiddenInteractionException.class, registrationProcess::startAndWaitForVerification);
    }

}
