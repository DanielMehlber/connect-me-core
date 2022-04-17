package org.connectme.core.tests.userManagement;


import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.tests.userManagement.testUtil.TestUserDataRepository;
import org.connectme.core.userManagement.UserManagement;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.UsernameAlreadyTakenException;
import org.connectme.core.userManagement.impl.jpa.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserManagementTest {

    @BeforeEach
    public void prepare() {
        userRepository.deleteAll();
    }

    @Autowired
    private UserManagement userManagement;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() throws InternalErrorException, UsernameAlreadyTakenException, NoSuchUserException {
        RegistrationUserData userdata = TestUserDataRepository.assembleValidRegistrationUserData();
        User user = new User(userdata);

        // persist new user
        userManagement.createNewUser(user);

        // check if user has been created
        User fetchedUser = userManagement.fetchUserByUsername(userdata.getUsername());
        Assertions.assertEquals(user, fetchedUser);

    }

    @Test
    public void testUpdateUser() throws InternalErrorException, UsernameAlreadyTakenException, NoSuchUserException {
        RegistrationUserData originalUserData = TestUserDataRepository.assembleValidRegistrationUserData();
        User user = new User(originalUserData);

        // persist new user
        userManagement.createNewUser(user);

        // create new user data that is different from the original one but with same username
        RegistrationUserData newUserData;
        do {
            newUserData = TestUserDataRepository.assembleValidRegistrationUserData();
            newUserData.setUsername(originalUserData.getUsername());
        } while (originalUserData.equals(newUserData)); // repeat if user data matches until it doesn't

        // update user data accordingly
        User newUser = new User(newUserData);
        userManagement.updateUserData(newUser);

        // check if user data has been changed
        User persistedUser = userManagement.fetchUserByUsername(originalUserData.getUsername());
        Assertions.assertEquals(newUser, persistedUser);
    }

    @Test
    public void testDeleteUser() throws InternalErrorException, UsernameAlreadyTakenException, NoSuchUserException {
        RegistrationUserData originalUserData = TestUserDataRepository.assembleValidRegistrationUserData();
        User user = new User(originalUserData);

        // persist new user
        userManagement.createNewUser(user);

        // delete user
        userManagement.deleteUser(user.getUsername());

        // check if the user really was deleted
        Assertions.assertThrows(NoSuchUserException.class, () -> userManagement.fetchUserByUsername(user.getUsername()));
    }

    @Test
    public void testIsUsernameAvailable() throws InternalErrorException, UsernameAlreadyTakenException {
        RegistrationUserData originalUserData = TestUserDataRepository.assembleValidRegistrationUserData();
        User user = new User(originalUserData);

        Assertions.assertTrue(userManagement.isUsernameAvailable(user.getUsername()));

        // persist new user
        userManagement.createNewUser(user);

        Assertions.assertFalse(userManagement.isUsernameAvailable(user.getUsername()));
    }


    // TODO: add more tests for interactions with users that do not exist
}
