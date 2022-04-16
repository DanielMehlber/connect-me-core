package org.connectme.core.userManagement.api;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.RegistrationVerificationNowAllowedException;
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

    @Autowired
    private StatefulRegistrationBean registration;

    /**
     * The client calls this method in order to init or reset a registration.
     *
     * @throws ForbiddenInteractionException This call is forbidden in the current registration state
     */
    @PostMapping("/users/registration/init")
    public void initRegistration() throws ForbiddenInteractionException {
        if(registration == null) {
            // CASE: no existing registration process for this session
            registration = new StatefulRegistrationBean();
        } else {
            // CASE: registration process already exists for this session
            registration.reset();
            //           ^^^^^ may throw ForbiddenInteractionException if reset is not allowed
        }
    }


    @PostMapping(value="/users/registration/set/userdata", consumes="application/json")
    public void uploadUserData(@RequestBody final RegistrationUserData userData) throws ForbiddenInteractionException, UserDataInsufficientException, InternalErrorException, UsernameAlreadyTakenException {
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        /*
         * setting user data in session bean (if interaction is even allowed) and checking if it is allowed by the
         * system (syntax, profanity, ...).
         *
         * The availability will be checked in the next step, not here yet.
         */
        registration.setUserData(userData);
        //           ^^^^^^^^^^^ may throw ForbiddenInteractionException, UsernameAlreadyTakenException

        /*
         * the availability of the username will be checked here.
         * It's checked after it is confirmed to be allowed in any other ways on purpose.
         */
        if(!userManagement.isUsernameAvailable(userData.getUsername())) {
            // if user data is invalid, reset to remove user data from registration
            registration.reset();
            throw new UsernameAlreadyTakenException(userData.getUsername());
        }
    }

    /**
     * The client calls this method in order to start the verification process and receive a verification code
     *
     * @throws ForbiddenInteractionException               this call is not allowed at the present moment
     * @throws RegistrationVerificationNowAllowedException a verification attempt is not allowed to the present moment
     */
    @PostMapping("/users/registration/start/verify")
    public void startVerificationProcess() throws ForbiddenInteractionException, RegistrationVerificationNowAllowedException {
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        // start verification process
        registration.startAndWaitForVerification();
        //           ^^^^^^^^^^^^^^^^^^^^^^^^^^^ may throw ForbiddenInteractionException, RegistrationVerificationNowAllowedException

        // TODO: send verification code via SMS (but only if not in testing mode)
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
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        // verify using code
        registration.checkVerificationCode(passedVerificationCode);
        //           ^^^^^^^^^^^^^^^^^^^^^ may throw WrongVerificationCodeException

        /*
         * create user from registration data and persist him in DB
         */
        final User newUser = new User(registration.getPassedUserData());
        userManagement.createNewUser(newUser);
        //             ^^^^^^^^^^^^^ may throw UsernameAlreadyTakenException
    }

}
