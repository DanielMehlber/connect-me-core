package org.connectme.core.userManagement.api;

import org.connectme.core.globalExceptions.ForbiddenInteractionException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.logic.StatefulLoginBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginAPI {

    @Autowired
    private StatefulLoginBean statefulLoginBean;

    @PostMapping("/users/login/init")
    public void init() throws ForbiddenInteractionException {
        statefulLoginBean.reset();
        //                ^^^^^ may throw ForbiddenInteractionException
    }

}
