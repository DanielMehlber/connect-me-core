package org.connectme.core.userManagement.entities;

import org.connectme.core.globalExceptions.InternalErrorException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
public class User {

    @Id @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String passwordHash;

    private User() {}

    /**
     * Creates new User from {@link RegistrationUserData} after registration process
     * has been completed.
     *
     * @param userdata user data from registration that will be converted
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

    private String hash(String password) throws NoSuchAlgorithmException {
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
}
