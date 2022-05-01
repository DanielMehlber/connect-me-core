package org.connectme.core.tests.userManagement.beans;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.tests.userManagement.testUtil.TestUserDataRepository;
import org.connectme.core.userManagement.beans.StatefulRegistrationBean;
import org.connectme.core.userManagement.entities.PassedUserData;
import org.connectme.core.userManagement.exceptions.PhoneNumberAlreadyInUseException;
import org.connectme.core.userManagement.exceptions.UserDataInsufficientException;
import org.connectme.core.userManagement.exceptions.VerificationAttemptNotAllowedException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.impl.jpa.UserRepository;
import org.connectme.core.userManagement.logic.RegistrationState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class StatefulRegistrationBeanTest {

    @Autowired
    private StatefulRegistrationBean statefulRegistrationBean;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void prepare() {
        userRepository.deleteAll();
    }

    @Test
    public void happyPath() throws VerificationAttemptNotAllowedException, WrongVerificationCodeException, ForbiddenInteractionException, UserDataInsufficientException, PhoneNumberAlreadyInUseException {
        /*
         * SCENARIO: go through happy path of registration process
         */

        PassedUserData userData = TestUserDataRepository.assembleValidPassedUserData();

        statefulRegistrationBean.reset();

        statefulRegistrationBean.setUserData(userData);
        Assertions.assertEquals(statefulRegistrationBean.getPassedUserData(), userData);


        statefulRegistrationBean.startAndWaitForVerification();

        String code = statefulRegistrationBean.getPhoneNumberVerification().getVerificationCode();
        statefulRegistrationBean.checkVerificationCode(code);

        Assertions.assertTrue(statefulRegistrationBean.getPhoneNumberVerification().isVerified());
        Assertions.assertSame(statefulRegistrationBean.getState(), RegistrationState.USER_VERIFIED);
    }

    @Test
    public void exceedVerificationLimit() throws VerificationAttemptNotAllowedException, WrongVerificationCodeException, ForbiddenInteractionException, UserDataInsufficientException, PhoneNumberAlreadyInUseException {
        /*
         * SCENARIO: evil or clumsy user enters wrong verification code too often and has to wait for a certain amount
         * of time. The amount of verification attempts per time has to be limited because SMS costs money.
         *
         * Test this security mechanism
         */

        statefulRegistrationBean.reset();

        statefulRegistrationBean.setUserData(TestUserDataRepository.assembleValidPassedUserData());

        // exceed max amount of allowed verifications attempts
        for (int i = 0; i < statefulRegistrationBean.getPhoneNumberVerification().getMaxVerificationAttempts(); i++) {
            statefulRegistrationBean.startAndWaitForVerification();
            try {
                statefulRegistrationBean.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(VerificationAttemptNotAllowedException.class, statefulRegistrationBean::startAndWaitForVerification);

        // reduce time to wait in order to complete unit test faster
        statefulRegistrationBean.getPhoneNumberVerification().setLastVerificationAttempt(LocalDateTime.now().minusMinutes(statefulRegistrationBean.getPhoneNumberVerification().getBlockedVerificationDurationMinutes()));

        // try again (this time with the correct code)
        statefulRegistrationBean.startAndWaitForVerification();
        String code = statefulRegistrationBean.getPhoneNumberVerification().getVerificationCode();
        statefulRegistrationBean.checkVerificationCode(code);
    }

    @Test
    public void attemptProcessRestartWhileVerificationBlock() throws VerificationAttemptNotAllowedException, ForbiddenInteractionException, UserDataInsufficientException, PhoneNumberAlreadyInUseException {
        /*
         * SCENARIO: evil user tries to send infinite verification SMS in order to harm us:
         * After he attempted too many verifications he must wait. To bypass that, he tries to reset the
         * registration process. This action is not allowed while the verification is "blocked".
         *
         * Test this security mechanism
         */

        statefulRegistrationBean.reset();

        statefulRegistrationBean.setUserData(TestUserDataRepository.assembleValidPassedUserData());

        // exceed max attempt of verifications
        for (int i = 0; i < statefulRegistrationBean.getPhoneNumberVerification().getMaxVerificationAttempts(); i++) {
            statefulRegistrationBean.startAndWaitForVerification();
            try {
                statefulRegistrationBean.checkVerificationCode("");
            } catch (final WrongVerificationCodeException ignored) {}
        }

        // attempt another verification right away and expect exception
        Assertions.assertThrows(VerificationAttemptNotAllowedException.class, statefulRegistrationBean::startAndWaitForVerification);

        // try to reset registration in order to illegally bypass blocked time. Must be interrupted by exception.
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::reset);
    }

    @Test
    public void attemptIllegalInteractionsToStates() throws WrongVerificationCodeException, VerificationAttemptNotAllowedException, ForbiddenInteractionException, UserDataInsufficientException, PhoneNumberAlreadyInUseException {
        /*
         * SCENARIO: In every state of the registration only certain interactions are allowed.
         *
         * Test that other interactions are not allowed
         */

        PassedUserData userData = TestUserDataRepository.assembleValidPassedUserData();

        // Set state to CREATED, following interactions are not allowed:
        statefulRegistrationBean.reset();
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
        statefulRegistrationBean.checkVerificationCode(statefulRegistrationBean.getPhoneNumberVerification().getVerificationCode());
        Assertions.assertThrows(ForbiddenInteractionException.class, () -> statefulRegistrationBean.setUserData(userData));
        Assertions.assertThrows(ForbiddenInteractionException.class, statefulRegistrationBean::startAndWaitForVerification);
    }

}
