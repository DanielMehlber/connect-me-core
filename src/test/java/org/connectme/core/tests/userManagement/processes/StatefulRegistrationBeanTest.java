package org.connectme.core.tests.userManagement.processes;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.tests.userManagement.testUtil.UserDataRepository;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.exceptions.RegistrationVerificationNowAllowedException;
import org.connectme.core.userManagement.exceptions.UserDataInsufficientException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.logic.StatefulRegistrationBean;
import org.connectme.core.userManagement.logic.RegistrationState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class StatefulRegistrationBeanTest {

    @Test
    public void happyPath() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException, ForbiddenInteractionException, UserDataInsufficientException {
        /*
         * SCENARIO: go through happy path of registration process
         */

        String username = UserDataRepository.Usernames.getRandomAllowed();
        String password = UserDataRepository.Passwords.randomAllowed();
        String phoneNumber = "0 000 000000";

        RegistrationUserData userData = new RegistrationUserData(username, password, phoneNumber);

        StatefulRegistrationBean statefulRegistrationBean = new StatefulRegistrationBean();

        statefulRegistrationBean.setUserData(userData);
        Assertions.assertEquals(statefulRegistrationBean.getPassedUserData(), userData);


        statefulRegistrationBean.startAndWaitForVerification();

        String code = statefulRegistrationBean.getVerificationCode();
        statefulRegistrationBean.checkVerificationCode(code);

        Assertions.assertTrue(statefulRegistrationBean.isVerified());
        Assertions.assertSame(statefulRegistrationBean.getState(), RegistrationState.USER_VERIFIED);
    }

    @Test
    public void exceedVerificationLimit() throws RegistrationVerificationNowAllowedException, WrongVerificationCodeException, ForbiddenInteractionException, UserDataInsufficientException {
        /*
         * SCENARIO: evil or clumsy user enters wrong verification code too often and has to wait for a certain amount
         * of time. The amount of verification attempts per time has to be limited because SMS costs money.
         *
         * Test this security mechanism
         */

        StatefulRegistrationBean statefulRegistrationBean = new StatefulRegistrationBean();

        statefulRegistrationBean.setUserData(UserDataRepository.assembleValidRegistrationUserData());

        // exceed max amount of allowed verifications attempts
        for (int i = 0; i <= StatefulRegistrationBean.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            statefulRegistrationBean.startAndWaitForVerification();
            try {
                statefulRegistrationBean.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, statefulRegistrationBean::startAndWaitForVerification);

        // reduce time to wait in order to complete unit test faster
        statefulRegistrationBean.setLastVerificationAttempt(LocalDateTime.now().minusMinutes(StatefulRegistrationBean.BLOCK_FAILED_ATTEMPT_MINUTES));

        // try again (this time with the correct code)
        statefulRegistrationBean.startAndWaitForVerification();
        String code = statefulRegistrationBean.getVerificationCode();
        statefulRegistrationBean.checkVerificationCode(code);
    }

    @Test
    public void attemptProcessRestartWhileVerificationBlock() throws RegistrationVerificationNowAllowedException, ForbiddenInteractionException, UserDataInsufficientException {
        /*
         * SCENARIO: evil user tries to send infinite verification SMS in order to harm us:
         * After he attempted too many verifications he must wait. To bypass that, he tries to reset the
         * registration process. This action is not allowed while the verification is "blocked".
         *
         * Test this security mechanism
         */

        StatefulRegistrationBean statefulRegistrationBean = new StatefulRegistrationBean();

        statefulRegistrationBean.setUserData(UserDataRepository.assembleValidRegistrationUserData());

        // exceed max attempt of verifications
        for (int i = 0; i <= StatefulRegistrationBean.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            statefulRegistrationBean.startAndWaitForVerification();
            try {
                statefulRegistrationBean.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(RegistrationVerificationNowAllowedException.class, statefulRegistrationBean::startAndWaitForVerification);

        // try to reset registration in order to illegally bypass blocked time. Must be interrupted by exception.
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::reset);
    }

    @Test
    public void attemptIllegalInteractionsToStates() throws WrongVerificationCodeException, RegistrationVerificationNowAllowedException, ForbiddenInteractionException, UserDataInsufficientException {
        /*
         * SCENARIO: In every state of the registration only certain interactions are allowed.
         *
         * Test that other interactions are not allowed
         */

        RegistrationUserData userData = UserDataRepository.assembleValidRegistrationUserData();

        // Set state to CREATED, following interactions are not allowed:
        StatefulRegistrationBean statefulRegistrationBean = new StatefulRegistrationBean();
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::startAndWaitForVerification);
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.checkVerificationCode(""));

        // Set state to USERNAME_PASSWORD_SET, following interactions are not allowed:
        statefulRegistrationBean.setUserData(userData);
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.setUserData(userData));
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.checkVerificationCode(""));

        // Set state to WAITING_FOR_PHONE_NUMBER_VERIFICATION, following interactions are not allowed:
        statefulRegistrationBean.startAndWaitForVerification();
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.setUserData(userData));
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::startAndWaitForVerification);

        // Set state to PHONE_NUMBER_VERIFIED, following interactions are not allowed:
        statefulRegistrationBean.checkVerificationCode(statefulRegistrationBean.getVerificationCode());
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.setUserData(userData));
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::startAndWaitForVerification);
    }

}
