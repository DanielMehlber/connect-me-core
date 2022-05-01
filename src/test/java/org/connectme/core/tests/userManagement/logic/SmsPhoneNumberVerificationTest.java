package org.connectme.core.tests.userManagement.logic;

import org.connectme.core.userManagement.exceptions.VerificationAttemptNotAllowedException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.logic.SmsPhoneNumberVerification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class SmsPhoneNumberVerificationTest {

    @Test
    public void happyPath() throws VerificationAttemptNotAllowedException, WrongVerificationCodeException {
        // -- arrange --
        SmsPhoneNumberVerification verification = new SmsPhoneNumberVerification();

        // -- act --
        verification.startVerificationAttempt();
        verification.checkVerificationCode(verification.getVerificationCode());

        // -- assert --
        Assertions.assertTrue(verification.isVerified());
    }

    @Test
    public void exceedVerificationAttemptLimit() throws Exception {
        // -- arrange --
        SmsPhoneNumberVerification verification = new SmsPhoneNumberVerification();

        // exceed verification limit
        for(int i = 0; i < SmsPhoneNumberVerification.MAX_AMOUNT_VERIFICATION_ATTEMPTS; i++) {
            verification.startVerificationAttempt();
            try {
                verification.checkVerificationCode("");
            } catch (WrongVerificationCodeException ignored) {}
        }

        // -- act and assert --
        Assertions.assertFalse(verification.isVerificationAttemptCurrentlyAllowed());
        Assertions.assertThrows(VerificationAttemptNotAllowedException.class, () -> verification.startVerificationAttempt());
        Assertions.assertFalse(verification.isVerified());
        // skip waiting time
        verification.setLastVerificationAttempt(LocalDateTime.now().minusMinutes(SmsPhoneNumberVerification.BLOCK_FAILED_ATTEMPT_MINUTES));
        // try again
        verification.startVerificationAttempt();
        verification.checkVerificationCode(verification.getVerificationCode());

        Assertions.assertTrue(verification.isVerified());

    }

    // TODO: test isVerificationAttemptAllowed: should not be allowed if a verification attempt is pending

}
