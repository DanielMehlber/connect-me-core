package org.connectme.core.userManagement.api;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.processes.RegistrationProcess;
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
    @PostMapping("/user/registration/init")
    public void initRegistration(HttpSession session) throws ForbiddenInteractionException {
        RegistrationProcess registration = (RegistrationProcess) session.getAttribute(SESSION_REGISTRATION);
        if(registration == null) {
            // CASE: no existing registration process for this session
            registration = new RegistrationProcess();

            // add registration to session
            session.setAttribute(SESSION_REGISTRATION, registration);
        } else {
            // CASE: registration process already exists for this session
            registration.reset();
            //           ^^^^^ may throw ForbiddenInteractionException if reset is not allowed
        }
    }

}
