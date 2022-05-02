package org.connectme.core.userManagement.beans;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.api.LoginAPI;
import org.connectme.core.userManagement.entities.PassedLoginData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.VerificationAttemptNotAllowedException;
import org.connectme.core.userManagement.exceptions.WrongPasswordException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.logic.LoginState;
import org.connectme.core.userManagement.logic.SmsPhoneNumberVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * This stateful bean is stored in session and holds all information and progress of
 * the users' login.
 */
@Component(LoginAPI.SESSION_LOGIN)
@SessionScope
public class StatefulLoginBean {

    @Autowired
    private UserManagement userManagement;

    /**
     * Current state of the process. Is checked in all interactions with an instance of this class
     */
    private LoginState state;

    /**
     * this instance manages the phone number verification via SMS
     */
    private SmsPhoneNumberVerification phoneNumberVerification;

    /**
     * Login data passed by the user
     */
    private PassedLoginData loginData;

    public StatefulLoginBean() {
        try {
            reset();
        } catch (ForbiddenInteractionException ignored) {}
    }

    /**
     * Reset and re-initialize this stateful bean if allowed. This interaction is not allowed if the verification
     * is currently blocked because otherwise the user could bypass the verification block.
     * @throws ForbiddenInteractionException resetting the bean is currently not allowed
     * @see StatefulLoginBean#isResetAllowed()
     * @author Daniel Mehlber
     */
    public void reset() throws ForbiddenInteractionException {
        if(!isResetAllowed()) {
            throw new ForbiddenInteractionException("reset is currently not allowed");
        } else {
            state = LoginState.INIT;
            phoneNumberVerification = new SmsPhoneNumberVerification();
        }
    }

    /**
     * Checks if the reset interaction is allowed at the present moment. A reset is not allowed if the verification
     * is currently blocked (otherwise the user could bypass the verification block).
     * @return true if verification is currently allowed
     * @see SmsPhoneNumberVerification#isVerificationAttemptCurrentlyAllowed()
     * @author Daniel Mehlber
     */
    private boolean isResetAllowed() {
        if(phoneNumberVerification != null)
            return phoneNumberVerification.isVerificationAttemptCurrentlyAllowed();
        return true;
    }


    /**
     * Checks login data (tries to fetch user with username and compares password-hashes) and stores it.
     *
     * @param passedLoginData user data passed by the user himself for login
     * @throws ForbiddenInteractionException this interaction is not allowed due to the current state
     * @throws NoSuchUserException username does not belong to a known user
     * @throws WrongPasswordException password for username is not current
     * @throws InternalErrorException some internal error occurred
     *
     * @see UserManagement#fetchUserByUsername(String)
     * @author Daniel Mehlber
     */
    public void passLoginData(final PassedLoginData passedLoginData) throws ForbiddenInteractionException, NoSuchUserException, WrongPasswordException, InternalErrorException {
        if(state != LoginState.INIT)
            throw new ForbiddenInteractionException(String.format("cannot pass login data because login is in state %s", state.name()));

        // try to load userdata
        User user = userManagement.fetchUserByUsername(passedLoginData.getUsername());

        // check if password is correct
        if(user.getPasswordHash().equals(passedLoginData.getPasswordHash())) {
            state = LoginState.CORRECT_LOGIN_DATA_PASSED;
            this.loginData = passedLoginData;
        } else {
            throw new WrongPasswordException();
        }
    }

    /**
     * Invokes state in which the passed phone number must be verified by user.
     * This state generates and sets the verification code.
     *
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws VerificationAttemptNotAllowedException another verification attempt is currently not allowed
     * @author Daniel Mehlber
     */
    public void startAndWaitForVerification() throws ForbiddenInteractionException, VerificationAttemptNotAllowedException {
        if (state != LoginState.CORRECT_LOGIN_DATA_PASSED)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot wait for phone number verification", state.name()));
        else {
            phoneNumberVerification.startVerificationAttempt();
            state = LoginState.VERIFICATION_PENDING;
        }
    }

    /**
     * This checks the verification code passed by the user and sets the state accordingly
     *
     * @param passedVerificationCode verification code that was passed by the user and needs to be checked
     * @throws ForbiddenInteractionException this instance is currently in a different state and awaits different interactions
     * @throws WrongVerificationCodeException wrong verification code passed by user
     * @author Daniel Mehlber
     */
    public void checkVerificationCode(final String passedVerificationCode) throws ForbiddenInteractionException, WrongVerificationCodeException {
        if(state != LoginState.VERIFICATION_PENDING)
            throw new ForbiddenInteractionException(
                    String.format("registration is in state %s and cannot accept verification codes", state.name()));
        else {
            // check verification code
            try {
                phoneNumberVerification.checkVerificationCode(passedVerificationCode);
            } catch (final WrongVerificationCodeException e) {
                // CASE: wrong verification code, user must reenter verification process
                state = LoginState.CORRECT_LOGIN_DATA_PASSED;
                throw e;
            }

            // CASE: correct verification code has been entered
            state = LoginState.PROFILE_VERIFIED;
        }

    }

    public LoginState getState() {
        return state;
    }

    public SmsPhoneNumberVerification getPhoneNumberVerification() {
        return phoneNumberVerification;
    }

    public PassedLoginData getLoginData() {
        return loginData;
    }
}
