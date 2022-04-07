package org.connectme.core.userManagement.impl.jpa;

import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.processes.RegistrationProcess;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.UsernameAlreadyTakenException;
import org.springframework.stereotype.Component;

@Component("JpaUserManagement")
public class JpaUserManagement implements UserManagement {
    @Override
    public boolean isUsernameAvailable(String username) throws RuntimeException, InternalErrorException {
        return false;
    }

    @Override
    public User fetchUserByUsername(String username) throws RuntimeException, InternalErrorException {
        return null;
    }

    @Override
    public void createNewUser(RegistrationProcess userdata) throws RuntimeException, InternalErrorException, UsernameAlreadyTakenException {

    }

    @Override
    public void updateUserData(User userdata) throws RuntimeException, InternalErrorException, NoSuchUserException {

    }

    @Override
    public void deleteUser(String username) throws RuntimeException, InternalErrorException, NoSuchUserException {

    }
}