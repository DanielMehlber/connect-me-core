package org.connectme.core.tests.userManagement.entites;

import org.connectme.core.tests.userManagement.testUtil.UserDataRepository;
import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.exceptions.PasswordTooWeakException;
import org.connectme.core.userManagement.exceptions.UsernameNotAllowedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistrationUserDataTest {

    @Test
    public void testCheckUsernamesSyntax() throws UsernameNotAllowedException {

        for(String allowed : UserDataRepository.Usernames.allowed) {
            RegistrationUserData.checkUsernameValue(allowed);
        }

        for(String forbidden : UserDataRepository.Usernames.forbidden) {
            Assertions.assertThrows(UsernameNotAllowedException.class, () -> RegistrationUserData.checkUsernameValue(forbidden));
        }

    }

    @Test
    public void testCheckUsernamesLength() throws UsernameNotAllowedException {

        // create string that is too short
        StringBuilder tooShortStringBuilder = new StringBuilder();
        for(int i = 0; i < RegistrationUserData.MIN_USERNAME_LENGTH - 1; i++)
            tooShortStringBuilder.append("a");
        String tooShortString = tooShortStringBuilder.toString();

        // create string that is too long
        StringBuilder tooLongStringBuilder = new StringBuilder();
        for(int i = 0; i <= RegistrationUserData.MAX_USERNAME_LENGTH; i++)
            tooLongStringBuilder.append("a");
        String tooLongString = tooLongStringBuilder.toString();

        // create string that has the right length
        int length = (RegistrationUserData.MIN_USERNAME_LENGTH + RegistrationUserData.MAX_USERNAME_LENGTH) / 2;
        StringBuilder correctStringBuilder = new StringBuilder();
        for(int i = 0; i < length; i++)
            correctStringBuilder.append("a");
        String correctString = correctStringBuilder.toString();

        Assertions.assertThrows(UsernameNotAllowedException.class, () -> RegistrationUserData.checkUsernameValue(tooShortString));
        Assertions.assertThrows(UsernameNotAllowedException.class, () -> RegistrationUserData.checkUsernameValue(tooLongString));
        RegistrationUserData.checkUsernameValue(correctString);

    }

    @Test
    public void testCheckPassword() throws PasswordTooWeakException {

        String username = "username";
        for(String allowed : UserDataRepository.Passwords.allowed) {
            RegistrationUserData.checkPasswordValue(allowed, username);
        }

        for(String forbidden : UserDataRepository.Passwords.forbidden) {
            Assertions.assertThrows(PasswordTooWeakException.class,
                    () -> RegistrationUserData.checkPasswordValue(forbidden, username));
        }

    }

    @Test
    public void attemptPasswordSameAsUsername() {
        Assertions.assertThrows(PasswordTooWeakException.class,
                () -> RegistrationUserData.checkPasswordValue("owurwoedaniel2sdfsdf", "daniel"));
    }

}
