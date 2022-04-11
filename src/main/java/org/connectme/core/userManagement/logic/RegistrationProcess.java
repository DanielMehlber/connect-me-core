package org.connectme.core.userManagement.logic;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.exceptions.RegistrationVerificationNowAllowedException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.entities.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Because of various verification steps along the registration process this application keeps the
 * registration data and state in the user's session.
 *
 * STORED IN SESSION (temporary object)
 *
 * An instance of this class will be converted to a {@link User} object after the registration process
 * is completed and persisted in the database.
 *
 * @author Daniel Mehlber
 */
@Component
@SessionScope
public class RegistrationProcess {

    public static final int MAX_AMOUNT_VERIFICATION_ATTEMPTS = 3;
    public static final int BLOCK_FAILED_ATTEMPT_MINUTES = 5;

    private RegistrationUserData passedUserData;

    /** generated verification code for phone number verification */
    private String verificationCode;

    /** amount of verification attempts. Used to limit amount of attempts per time */
    private int verificationAttempts;

    /** last verification attempt. Used to allow only a certain amount of attempts per time */
    private LocalDateTime lastVerificationAttempt;

    /** is registration verified */
    private boolean verified;

    /**
     * The registration process has multiple steps along which the client updates the instances state
     * e.g. before and after phone number verification
     */
    private RegistrationProcessState state;

    /**
     * Creates new Registration and sets state accordingly
     * @author Daniel Mehlber
     */
    public RegistrationProcess() {
        try {
            reset();
        } catch (ForbiddenInteractionException ignored) {}
    }

    /**
     * Invokes state after a valid username and secure password have been entered.
     *
     * PREVIOUS STEP: Registration has been created
     * NEXT STEP: user will enter his phone number
     *
     * @param passedUserData user data passed by the user himself
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @author Daniel Mehlber
     */
    public void setUserData(final RegistrationUserData passedUserData) throws ForbiddenInteractionException {
        if(state != RegistrationProcessState.CREATED)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot accept username/password", state.name()));
        else {
            this.passedUserData = passedUserData;
            state = RegistrationProcessState.USER_DATA_PASSED;
        }
    }


    /**
     * Invokes state in which the passed phone number must be verified by user.
     * This state generates and sets the verification code.
     *
     * PREVIOUS STEPS:
     *  - phone number has been set (first attempt)
     *  - previous verification has failed (new attempt)
     * NEXT STEP : user has entered verification code, check it
     *
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws RegistrationVerificationNowAllowedException another registration is currently not allowed
     * @author Daniel Mehlber
     */
    public void startAndWaitForVerification() throws ForbiddenInteractionException, RegistrationVerificationNowAllowedException {
        if (state != RegistrationProcessState.USER_DATA_PASSED)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot wait for phone number verification", state.name()));
        else {

            // check if time window has passed and a new attempt is allowed
            if(isVerificationAttemptCurrentlyAllowed()) {
                // CASE: verification attempt is allowed
                this.verificationCode = generateVerificationCode();
                state = RegistrationProcessState.WAITING_FOR_PHONE_NUMBER_VERIFICATION;

                // TODO: send verification code via SMS (but only if not in testing mode)
            } else {
                // CASE: not enough time has passed, prohibit another verification attempt
                throw new RegistrationVerificationNowAllowedException();
            }

        }
    }

    /**
     * Checks if another verification attempt is allowed at the present moment.
     * If there are too many failed attempts the user has to wait a certain time.
     *
     * @return if another verification attempt is allowed right now
     * @author Daniel Mehlber
     */
    private boolean isVerificationAttemptCurrentlyAllowed() {
        final LocalDateTime now = LocalDateTime.now();
        if(verificationAttempts > MAX_AMOUNT_VERIFICATION_ATTEMPTS) {
            // CASE: max limit for verification attempts was exceeded
            if(lastVerificationAttempt.plusMinutes(BLOCK_FAILED_ATTEMPT_MINUTES).isBefore(now)) {
                // CASE: enough time has passed, allow more attempts
                verificationAttempts = 0;
                return true;
            } else {
                // CASE: not enough time has passed, prohibit another verification attempt
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Checks if a process restart (=reset of the registration object) is allowed at the present moment.
     * @return if the reset is allowed.
     * @author Daniel Mehlber
     */
    private boolean isResetAllowed() {
        /*
         * SCENARIO: hacker/user attempted to many failed phone number verifications and now has to wait
         * a certain amount of time. He now tries to circumvent this mechanism by restarting the entire registration process
         * (=resetting this registration object).
         * We cannot allow infinite verification attempts because 1 Verification = 1 SMS = Money.
         *
         * If a verification attempt is currently not allowed the user must wait and a reset is not allowed.
         */
        return isVerificationAttemptCurrentlyAllowed();
    }

    /**
     * Generates random verification code
     * @return verification code
     */
    private String generateVerificationCode() {
        // TODO: Is this the best way?
        return String.valueOf(new Random().nextInt(99999));
    }

    /**
     * This checks the verification code passed by the user and sets the state accordingly.
     *
     * PREVIOUS STEP: verification code has been generated
     * NEXT STEPS:
     *  - if correct verification code: profile has been verified
     *  - if incorrect verification code: repeat verification process
     *
     * @param passedVerificationCode verification code that was passed by the user and needs to be checked
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws WrongVerificationCodeException wrong verification code passed by user
     * @author Daniel Mehlber
     */
    public void checkVerificationCode(final String passedVerificationCode) throws ForbiddenInteractionException, WrongVerificationCodeException {
        if(state != RegistrationProcessState.WAITING_FOR_PHONE_NUMBER_VERIFICATION)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot accept verification codes", state.name()));
        else {

            verificationAttempts++;
            lastVerificationAttempt = LocalDateTime.now();

            // check if passed verification code is correct
            if(verificationCode.equals(passedVerificationCode)) {
                // CASE: correct verification code has been entered
                verified = true;
                state = RegistrationProcessState.USER_VERIFIED;
            } else {
                // CASE: wrong verification code, user must reenter verification process
                state = RegistrationProcessState.USER_DATA_PASSED;
                throw new WrongVerificationCodeException(passedVerificationCode);
            }
        }

    }

    /**
     * This resets the object stored in the session and is called.
     *
     * IMPORTANT: This must be called if the registration process is (re-)starting, but there already is
     * a registration object stored in session. In certain states a reset is not allowed at the present moment
     * (e.g. too many failed verification attempts: the user must wait a certain amount of time)
     *
     * @throws ForbiddenInteractionException cannot reset registration object right now.
     * @author Daniel Mehlber
     */
    public void reset() throws ForbiddenInteractionException {
        if(isResetAllowed()) {
            verificationAttempts = 0;
            lastVerificationAttempt = null;
            state = RegistrationProcessState.CREATED;
            verified = false;
            passedUserData = null;
            verificationCode = null;
        } else {
            throw new ForbiddenInteractionException("a reset is currently not allowed/blocked");
        }
    }

    public RegistrationUserData getPassedUserData() {
        return passedUserData;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public boolean isVerified() {
        return verified;
    }

    public RegistrationProcessState getState() {
        return state;
    }

    /**
     * Only for testing purposes
     * @param time new time
     */
    public void setLastVerificationAttempt(final LocalDateTime time) {
        lastVerificationAttempt = time;
    }

}
