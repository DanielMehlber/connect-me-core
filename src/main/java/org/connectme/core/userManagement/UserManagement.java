package org.connectme.core.userManagement;

import org.connectme.core.globalExceptions.InternalErrorException;
import org.connectme.core.userManagement.entities.User;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.connectme.core.userManagement.exceptions.UsernameAlreadyTakenException;
import org.connectme.core.userManagement.impl.jpa.JpaUserManagement;

/**
 * This interface defines all actions considered user management that need to be accessed by APIs or components
 */
public interface UserManagement {

    default UserManagement newInstance() {
        return new JpaUserManagement();
    }

    /**
     * Checks if username is currently available at the present moment.
     * This is no guarantee that username will be available the next time it gets checked, no data is written.
     *
     * @param username will be searched in database
     * @return true, if this username is not already taken.
     * @throws RuntimeException any unexpected or unhandled errors that must be escalated
     * @throws InternalErrorException possible internal errors because of database connection
     */
    boolean isUsernameAvailable(final String username) throws RuntimeException, InternalErrorException;

    /**
     * Fetch User data belonging to a specific username.
     *
     * @param username ID of user data
     * @return user data, if there is any belonging to this username
     * @throws RuntimeException any unexpected or unhandled errors that must be escalated
     * @throws InternalErrorException possible internal errors e.g. because of database connection
     */
    User fetchUserByUsername(final String username) throws RuntimeException, InternalErrorException;

    /**
     * Create new user and persist it
     *
     * @param userdata data of user with available username
     * @throws RuntimeException any other unexpected errors that must be handled
     * @throws InternalErrorException possible internal errors e.g. because of database connection
     * @throws UsernameAlreadyTakenException user with this username cannot be created, username is not available
     */
    void createNewUser(final User userdata) throws RuntimeException, InternalErrorException, UsernameAlreadyTakenException;

    /**
     * Update user data
     * @param userdata user data containing username
     * @throws RuntimeException any other unexpected errors that must be handled
     * @throws InternalErrorException possible internal errors e.g. because of database connection
     * @throws NoSuchUserException no such user with passed username
     */
    void updateUserData(final User userdata) throws RuntimeException, InternalErrorException, NoSuchUserException;

    /**
     * Delete user and all its data
     * @param username ID of user
     * @throws RuntimeException any other unexpected errors that must be handled
     * @throws InternalErrorException possible internal errors e.g. because of database connection
     * @throws NoSuchUserException no such user with passed username
     */
    void deleteUser(final String username) throws RuntimeException, InternalErrorException, NoSuchUserException;
}
