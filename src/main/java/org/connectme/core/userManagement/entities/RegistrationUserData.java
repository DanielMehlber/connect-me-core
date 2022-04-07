package org.connectme.core.userManagement.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * This class contains all user data passed by the user himself mid-registration.
 * When any setter is accessed, the data is automatically checked. This is because the user cannot be trusted.
 */
public class RegistrationUserData {

    /** username suggestion of user (will not be reserved) */
    private String username;

    /** password in clear text format */
    private String password;

    /** telephone number of user */
    private String phoneNumber;

    /*
     * THIS CONSTRUCTOR IS MEANT TO BE PRIVATE
     * This default constructor is private because it is used to deserialize an object from JSON
     * and therefore is used only by the jackson library (which has private access to this class).
     * In any other scenario there is no use for a default constructor (with no arguments) and therefore
     * public access is prohibited.
     */
    private RegistrationUserData() {}

    /**
     * This constructor is used to deserialize the JSON string passed by the user.
     * @param username username of user (unchecked)
     * @param password password of user (unchecked)
     * @param phoneNumber phoneNumber of user (unchecked)
     */
    @JsonCreator
    public RegistrationUserData(@JsonProperty final String username,
                                @JsonProperty final String password,
                                @JsonProperty final String phoneNumber) {
        setUsername(username);
        setPassword(password);
        setPhoneNumber(phoneNumber);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        // TODO: Check username
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        // TODO: Check password strength
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        // TODO: Set password syntax
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationUserData that = (RegistrationUserData) o;
        return Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getPhoneNumber(), that.getPhoneNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPassword(), getPhoneNumber());
    }
}
