package org.connectme.core.tests.userManagement.entites;

import org.connectme.core.userManagement.entities.RegistrationUserData;
import org.connectme.core.userManagement.exceptions.PasswordTooWeakException;
import org.connectme.core.userManagement.exceptions.UsernameNotAllowedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistrationUserDataTest {

    private final String[] allowedUsernames = {
            "someusername",
            "some_user_name",
            "username123",
            "Us3rNaM3",
            "the_underscore_user"
    };

    // TODO: ADD Profane user names
    private final String[] forbiddenUsernames = {
            "white space",
            "multi\nline",
            "tabs\ttabs",
            "excamation!",
            "question?",
            "point.syntax",
            "list,list,list",
            "</hacker>",
    };

    private final String[] allowedPasswords = {
            "kajfhlaksjfh394857345h3kjh",
            "s8d7f6s8d7f6s8d7fs8d7f",
            "wrl645pha"
    };

    private final String[] forbiddenPasswords = {
            "hallo",
            "1234",
            "password3"
    };

    @Test
    public void testCheckUsernamesSyntax() throws UsernameNotAllowedException {

        for(String allowed : allowedUsernames) {
            RegistrationUserData.checkUsernameValue(allowed);
        }

        for(String forbidden : forbiddenUsernames) {
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
        for(String allowed : allowedPasswords) {
            RegistrationUserData.checkPasswordValue(allowed, username);
        }

        for(String forbidden : forbiddenPasswords) {
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
