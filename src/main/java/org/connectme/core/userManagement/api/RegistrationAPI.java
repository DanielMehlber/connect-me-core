package org.connectme.core.userManagement.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.VerificationAttemptNotAllowedException;
import org.connectme.core.userManagement.exceptions.UserDataInsufficientException;
import org.connectme.core.userManagement.exceptions.UsernameAlreadyTakenException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.logic.StatefulRegistrationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
public class RegistrationAPI {

    @Autowired
    UserManagement userManagement;

    public static final String SESSION_REGISTRATION = "session-registration";

    private final Logger log = LogManager.getLogger(RegistrationAPI.class);

    @Autowired
    private StatefulRegistrationBean registration;

    /**
     * The client calls this method in order to init or reset a registration.
     *
     * @throws ForbiddenInteractionException This call is forbidden in the current registration state
     */
    @PostMapping("/users/registration/init")
    public void initRegistration() throws ForbiddenInteractionException {
        log.debug("registration initialization requested");
        try {
            registration.reset();
        } catch (final ForbiddenInteractionException e) {
            log.warn("registration re-initialization currently not allowed");
            throw e;
        }
        log.info("registration (re-)initialized and reset");
    }


    @PostMapping(value="/users/registration/set/userdata", consumes="application/json")
    public void uploadUserData(@RequestBody final RegistrationUserData userData) throws ForbiddenInteractionException, UserDataInsufficientException, InternalErrorException, UsernameAlreadyTakenException {
        log.debug("user data for registration received");
        /*
         * setting user data in session bean (if interaction is even allowed) and checking if it is allowed by the
         * system (syntax, profanity, ...).
         *
         * The availability will be checked in the next step, not here yet.
         */
        try { // catch internal errors
            try {
                registration.setUserData(userData);
            } catch (ForbiddenInteractionException e) {
                log.warn("user data upload denied: " + e.getMessage());
                throw e;
            } catch (UserDataInsufficientException e) {
                log.warn("user data rejected: " + e.getMessage());
                throw e;
            }


            /*
             * the availability of the username will be checked here.
             * It's checked after it is confirmed to be allowed in any other ways on purpose.
             */
            if (!userManagement.isUsernameAvailable(userData.getUsername())) {
                log.warn("user data upload failed: username is already taken and therefor not available");
                // if user data is invalid, reset to remove user data from registration
                registration.reset();
                throw new UsernameAlreadyTakenException();
            }
        } catch (InternalErrorException e) {
            log.fatal("user data upload for registration failed due to an internal error", e);
            throw e;
        }

        log.info("user data for registration received");
    }

    /**
     * The client calls this method in order to start the verification process and receive a verification code
     *
     * @throws ForbiddenInteractionException               this call is not allowed at the present moment
     * @throws VerificationAttemptNotAllowedException a verification attempt is not allowed to the present moment
     */
    @PostMapping("/users/registration/start/verify")
    public void startVerificationProcess() throws ForbiddenInteractionException, VerificationAttemptNotAllowedException {
        log.debug("start of phone number verification process requested");
        try {
            // start verification process
            registration.startAndWaitForVerification();
        } catch (ForbiddenInteractionException e) {
            log.warn("start of phone number verification process denied: " + e.getMessage());
            throw e;
        } catch (VerificationAttemptNotAllowedException e) {
            log.warn("start of another verification attempt denied: " + e.getMessage());
            throw e;
        }


        // TODO: send verification code via SMS (but only if not in testing mode)
        log.info("phone number verification process started for registration");
    }

    /**
     * The client calls this method in order to check the received verification code and to complete the verification.
     *
     * @param passedVerificationCode the verification code passed by the user (unchecked)
     * @throws ForbiddenInteractionException  this call is not allowed at the present moment
     * @throws WrongVerificationCodeException the passed verification code is not correct
     */
    @PostMapping(value="/users/registration/verify", consumes="text/plain")
    public void verifyWithCode(@RequestBody final String passedVerificationCode) throws ForbiddenInteractionException, WrongVerificationCodeException, InternalErrorException, UsernameAlreadyTakenException {
        log.debug("phone number verification code received");
        // verify using code
        try {
            registration.checkVerificationCode(passedVerificationCode);
        } catch (ForbiddenInteractionException e) {
            log.warn("phone number verification not allowed: " + e.getMessage());
            throw e;
        } catch (WrongVerificationCodeException e) {
            log.warn("verification unsuccessful: passed verification code is not correct");
            throw e;
        }

        log.info("phone number verification successful");
        try {
            /*
             * create user from registration data and persist him in DB
             */
            final User newUser = new User(registration.getPassedUserData());
            userManagement.createNewUser(newUser);
            //             ^^^^^^^^^^^^^ may throw UsernameAlreadyTakenException
        } catch (InternalErrorException e) {
            log.warn("cannot create new user due to an internal error: " + e.getMessage());
            throw e;
        } catch (UsernameAlreadyTakenException e) {
            log.warn("cannot create new user: username is already taken");
            throw e;
        }

        log.info("created new user in database successfully");
    }

}
