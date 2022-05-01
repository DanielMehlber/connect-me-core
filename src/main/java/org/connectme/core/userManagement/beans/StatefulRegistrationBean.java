package org.connectme.core.userManagement.beans;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.api.RegistrationAPI;
import org.connectme.core.userManagement.entities.PassedUserData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.*;
import org.connectme.core.userManagement.impl.jpa.UserRepository;
import org.connectme.core.userManagement.logic.RegistrationState;
import org.connectme.core.userManagement.logic.SmsPhoneNumberVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

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
@Component(RegistrationAPI.SESSION_REGISTRATION)
@SessionScope
public class StatefulRegistrationBean {

    @Autowired
    private UserRepository userRepository;

    private PassedUserData passedUserData;

    /**
     * Holds all data and progress associated with the two-factor phone number verification.
     */
    private SmsPhoneNumberVerification phoneNumberVerification;

    /**
     * The registration process has multiple steps along which the client updates the instances state
     * e.g. before and after phone number verification
     */
    private RegistrationState state;

    /**
     * Creates new Registration and sets state accordingly
     * @author Daniel Mehlber
     */
    public StatefulRegistrationBean() {
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
     * @param passedUserData user data passed by the user himself. It will be checked in this step.
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws UserDataInsufficientException the user data was checked and declared insufficient or invalid
     * @author Daniel Mehlber
     * @see PassedUserData#check()
     */
    public void setUserData(final PassedUserData passedUserData) throws ForbiddenInteractionException, UserDataInsufficientException, PhoneNumberAlreadyInUseException {
        if(state != RegistrationState.CREATED)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot accept user data", state.name()));
        else {
            // perform value check
            try {
                passedUserData.check();

                // check if phone number is already in use by another user
                if(userRepository.existsByPhoneNumber(passedUserData.getPhoneNumber())) {
                    throw new PhoneNumberAlreadyInUseException();
                }
            } catch (final PasswordTooWeakException | UsernameNotAllowedException | PhoneNumberInvalidException reason) {
                throw new UserDataInsufficientException(reason);
            }

            this.passedUserData = passedUserData;
            state = RegistrationState.USER_DATA_PASSED;
        }
    }


    /**
     * Invokes state in which the passed phone number must be verified by user.
     * This state generates and sets the verification code.
     *
     * PREVIOUS STEPS:
     * - phone number has been set (first attempt)
     * - previous verification has failed (new attempt)
     *
     * NEXT STEP : user has entered verification code, check it
     *
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws VerificationAttemptNotAllowedException another registration is currently not allowed
     * @author Daniel Mehlber
     */
    public void startAndWaitForVerification() throws ForbiddenInteractionException, VerificationAttemptNotAllowedException {
        if (state != RegistrationState.USER_DATA_PASSED)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot wait for phone number verification", state.name()));
        else {

            phoneNumberVerification.startVerificationAttempt();
            state = RegistrationState.WAITING_FOR_PHONE_NUMBER_VERIFICATION;

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
        if(phoneNumberVerification != null)
            return phoneNumberVerification.isVerificationAttemptCurrentlyAllowed();
        else
            return true;
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
        if(state != RegistrationState.WAITING_FOR_PHONE_NUMBER_VERIFICATION)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot accept verification codes", state.name()));
        else {
            // check verification code
            try {
                phoneNumberVerification.checkVerificationCode(passedVerificationCode);
            } catch (final WrongVerificationCodeException e) {
                // CASE: wrong verification code, user must reenter verification process
                state = RegistrationState.USER_DATA_PASSED;
                throw e;
            }

            // CASE: correct verification code has been entered
            state = RegistrationState.USER_VERIFIED;
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
     * @see StatefulRegistrationBean#isResetAllowed()
     */
    public void reset() throws ForbiddenInteractionException {
        if(isResetAllowed()) {
            phoneNumberVerification = new SmsPhoneNumberVerification();
            state = RegistrationState.CREATED;
            passedUserData = null;
        } else {
            throw new ForbiddenInteractionException("a reset is currently not allowed/blocked");
        }
    }

    public PassedUserData getPassedUserData() {
        return passedUserData;
    }

    public RegistrationState getState() {
        return state;
    }

    public SmsPhoneNumberVerification getPhoneNumberVerification() {
        return phoneNumberVerification;
    }

}
