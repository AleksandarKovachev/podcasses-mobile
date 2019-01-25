package com.podcasses.model.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by aleksandar.kovachev.
 */
public class Account extends BaseObservable {

    private String username;

    private String password;

    private String keycloakId;

    private String email;

    private String firstName;

    private String lastName;

    private int emailVerified;

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Bindable
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Bindable
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(int emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void consume(Account account) {
        this.username = account.getUsername();
        this.password = account.password;
        this.email = account.getEmail();
        this.keycloakId = account.keycloakId;
        this.firstName = account.firstName;
        this.lastName = account.lastName;
        this.emailVerified = account.emailVerified;
        notifyChange();
    }

}
