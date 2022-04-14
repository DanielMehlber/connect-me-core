package org.connectme.core.tests.userManagement.testUtil;

import org.connectme.core.userManagement.entities.RegistrationUserData;

import java.util.Random;

/**
 * This util class contains a repository of usernames and password (both allowed and forbidden) that can be used
 * by various test scenarios.
 */
public class UserDataRepository {

    /**
     * Repository of usernames (both allowed and forbidden) for various testing scenarios
     */
    public static class Usernames {

        /**
         * list of allowed usernames
         */
        public static final String[] allowed = {
                "mb_nator",
                "username123",
                "davewasalreadytaken",
                "the_unserscore_user"
        };

        /**
         * list of forbidden usernames
         */
        public static final String[] forbidden = {
                "white space",
                "multi\nline",
                "tabs\ttabs",
                "excamation!",
                "question?",
                "point.syntax",
                "list,list,list",
                "</hacker>",
        };


        /**
         * selects a random allowed username from list and returns it
         * @return a random username that is allowed by the system
         */
        public static String getRandomAllowed() {
            int randomIndex = new Random().nextInt(allowed.length);
            return allowed[randomIndex];
        }

        /**
         * selects a random forbidden username from list and returns it
         * @return a random username that is forbidden by the system
         */
        public static String getRandomForbidden() {
            int randomIndex = new Random().nextInt(forbidden.length);
            return forbidden[randomIndex];
        }

    }

    public static class Passwords {

        public static final String[] allowed = {
                "kajfhlaksjfh394857345h3kjh",
                "s8d7f6s8d7f6s8d7fs8d7f",
                "wrl645pha"
        };

        public static final String[] forbidden = {
                "hallo",
                "1234",
                "password3"
        };

        public static String randomAllowed() {
            int randomIndex = new Random().nextInt(allowed.length);
            return allowed[randomIndex];
        }

        public static String randomForbidden() {
            int randomIndex = new Random().nextInt(forbidden.length);
            return forbidden[randomIndex];
        }
    }

    public static final RegistrationUserData assembleValidRegistrationUserData() {
        return new RegistrationUserData(UserDataRepository.Usernames.getRandomAllowed(), UserDataRepository.Passwords.randomAllowed(), "0 0000 00000");
    }

}
