package org.connectme.core.userManagement.entities;

import org.connectme.core.globalExceptions.InternalErrorException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * user data as it is stored in the database
 */
@Entity
public class User {

    @Id @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String passwordHash;

    /*
     * THIS DEFAULT CONSTRUCTOR IS MEANT TO BE PRIVATE
     * A default constructor is required by JPA.
     */
    @SuppressWarnings("unused")
    private User() {}

    /**
     * Creates new User from {@link RegistrationUserData} after registration process
     * has been completed.
     *
     * @param userdata user data from registration that will be converted. It will not be checked in this method, you have
     *                 to call {@link RegistrationUserData#check()} before.
     * @throws InternalErrorException password hashing was not successful
     */
    public User(final RegistrationUserData userdata) throws InternalErrorException {
        this.username = userdata.getUsername();
        try {
            this.passwordHash = hash(userdata.getPassword());
        } catch (NoSuchAlgorithmException e) {
            throw new InternalErrorException("cannot create hash of password", e);
        }
    }


    private static String hash(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        return new String(encodedHash);
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getUsername(), user.getUsername()) && Objects.equals(getPasswordHash(), user.getPasswordHash());
    }

}
