package org.connectme.core.userManagement.api;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.exceptions.RegistrationVerificationNowAllowedException;
import org.connectme.core.userManagement.exceptions.UserDataInsufficientException;
import org.connectme.core.userManagement.exceptions.WrongVerificationCodeException;
import org.connectme.core.userManagement.logic.StatefulRegistrationBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class RegistrationAPI {

    public static final String SESSION_REGISTRATION = "session-registration";

    /**
     * The client calls this method in order to init or reset a registration.
     *
     * @param session session in which the registration is stored
     * @throws ForbiddenInteractionException This call is forbidden in the current registration state
     */
    @PostMapping("/users/registration/init")
    public void initRegistration(HttpSession session) throws ForbiddenInteractionException {
        StatefulRegistrationBean registration = (StatefulRegistrationBean) session.getAttribute(SESSION_REGISTRATION);
        if(registration == null) {
            // CASE: no existing registration process for this session
            registration = new StatefulRegistrationBean();

            // add registration to session
            session.setAttribute(SESSION_REGISTRATION, registration);
        } else {
            // CASE: registration process already exists for this session
            registration.reset();
            //           ^^^^^ may throw ForbiddenInteractionException if reset is not allowed
        }
    }

    /**
     * The client calls this method after the registration has been initialized in order to upload
     * the required user data.
     * @param userData userdata passed by user
     * @param session session in which the registration is stored
     * @throws ForbiddenInteractionException this call is not allowed at the present moment
     * @throws UserDataInsufficientException the passed user data cannot be accepted by the system for some reason
     */
    @PostMapping(value="/users/registration/set/userdata", consumes="application/json")
    public void uploadUserData(HttpSession session, final RegistrationUserData userData) throws ForbiddenInteractionException, UserDataInsufficientException {
        StatefulRegistrationBean registration = (StatefulRegistrationBean) session.getAttribute(SESSION_REGISTRATION);
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        // set user data in session
        registration.setUserData(userData);
        //           ^^^^^^^^^^^ may throw ForbiddenInteractionException
    }

    /**
     * The client calls this method in order to start the verification process and receive a verification code
     *
     * @param session session in which the registration is stored
     * @throws ForbiddenInteractionException this call is not allowed at the present moment
     * @throws RegistrationVerificationNowAllowedException a verification attempt is not allowed to the present moment
     */
    @PostMapping("/users/registration/start/verify")
    public void startVerificationProcess(HttpSession session) throws ForbiddenInteractionException, RegistrationVerificationNowAllowedException {
        StatefulRegistrationBean registration = (StatefulRegistrationBean) session.getAttribute(SESSION_REGISTRATION);
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        // start verification process
        registration.startAndWaitForVerification();
        //           ^^^^^^^^^^^^^^^^^^^^^^^^^^^ may throw ForbiddenInteractionException, RegistrationVerificationNowAllowedException
    }

    /**
     * The client calls this method in order to check the received verification code and to complete the verification.
     *
     * @param session the session the registration is stored in
     * @param passedVerificationCode the verification code passed by the user (unchecked)
     * @throws ForbiddenInteractionException this call is not allowed at the present moment
     * @throws WrongVerificationCodeException the passed verification code is not correct
     */
    @PostMapping(value="/users/registration/verify", consumes="text/plain")
    public void verifyWithCode(HttpSession session, final String passedVerificationCode) throws ForbiddenInteractionException, WrongVerificationCodeException {
        StatefulRegistrationBean registration = (StatefulRegistrationBean) session.getAttribute(SESSION_REGISTRATION);
        if(registration == null)
            throw new ForbiddenInteractionException("No registration found in session");

        // verify using code
        registration.checkVerificationCode(passedVerificationCode);
        //           ^^^^^^^^^^^^^^^^^^^^^ may throw WrongVerificationCodeException
    }

}
