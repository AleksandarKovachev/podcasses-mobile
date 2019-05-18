package com.podcasses.model.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class Account {

    @NonNull
    @PrimaryKey
    private String id;

    private String username;

    private String displayName;

    private String email;

    private String firstName;

    private String lastName;

    private String description;

    private Integer categoryId;

    private Integer countryId;

    private String city;

    private Integer languageId;

    private String rssFeed;

    private Integer rssFeedVerified;

    private String rssFeedEmail;

    private int emailVerified;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getRssFeed() {
        return rssFeed;
    }

    public void setRssFeed(String rssFeed) {
        this.rssFeed = rssFeed;
    }

    public Integer getRssFeedVerified() {
        return rssFeedVerified;
    }

    public void setRssFeedVerified(Integer rssFeedVerified) {
        this.rssFeedVerified = rssFeedVerified;
    }

    public String getRssFeedEmail() {
        return rssFeedEmail;
    }

    public void setRssFeedEmail(String rssFeedEmail) {
        this.rssFeedEmail = rssFeedEmail;
    }

    public int getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(int emailVerified) {
        this.emailVerified = emailVerified;
    }
}
