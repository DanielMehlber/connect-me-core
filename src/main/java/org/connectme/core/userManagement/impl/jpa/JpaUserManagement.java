package org.connectme.core.userManagement.impl.jpa;

import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.UsernameAlreadyTakenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("JpaUserManagement")
@Primary
public class JpaUserManagement implements UserManagement {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean isUsernameAvailable(String username) throws RuntimeException, InternalErrorException {
        try {
            return !userRepository.existsById(username);
        } catch (RuntimeException e) {
            throw new InternalErrorException("cannot check if username is available", e);
        }
    }

    @Override
    public User fetchUserByUsername(String username) throws RuntimeException, InternalErrorException {
        return null;
    }

    @Override
    public void createNewUser(User userdata) throws RuntimeException, InternalErrorException, UsernameAlreadyTakenException {
        try {
            if (userRepository.existsById(userdata.getUsername()))
                throw new UsernameAlreadyTakenException(userdata.getUsername());
            else
                userRepository.save(userdata);
        } catch (RuntimeException e) {
            throw new InternalErrorException("cannot create new user", e);
        }
    }

    @Override
    public void updateUserData(User userdata) throws RuntimeException, InternalErrorException, NoSuchUserException {

    }

    @Override
    public void deleteUser(String username) throws RuntimeException, InternalErrorException, NoSuchUserException {

    }
}
