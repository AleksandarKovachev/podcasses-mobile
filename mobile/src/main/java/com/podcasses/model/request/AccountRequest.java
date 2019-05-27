package com.podcasses.model.request;

import com.podcasses.model.entity.Account;

/**
 * Created by aleksandar.kovachev.
 */
public class AccountRequest {

    private String username;

    private String displayName;

    private String firstName;

    private String lastName;

    private String description;

    private String email;

    private Integer countryId;

    private String city;

    private Integer languageId;

    private Integer categoryId;

    private String rssFeed;

    private String rssFeedEmail;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getRssFeed() {
        return rssFeed;
    }

    public void setRssFeed(String rssFeed) {
        this.rssFeed = rssFeed;
    }

    public String getRssFeedEmail() {
        return rssFeedEmail;
    }

    public void setRssFeedEmail(String rssFeedEmail) {
        this.rssFeedEmail = rssFeedEmail;
    }

    public static AccountRequest toAccountRequest(Account account) {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setUsername(account.getUsername());
        accountRequest.setRssFeedEmail(account.getRssFeedEmail());
        accountRequest.setRssFeed(account.getRssFeed());
        accountRequest.setLanguageId(account.getLanguageId());
        accountRequest.setCountryId(account.getCountryId());
        accountRequest.setCategoryId(account.getCategoryId());
        accountRequest.setFirstName(account.getFirstName());
        accountRequest.setLastName(account.getLastName());
        accountRequest.setEmail(account.getEmail());
        accountRequest.setCity(account.getCity());
        accountRequest.setDescription(account.getDescription());
        accountRequest.setDisplayName(account.getDisplayName());
        return accountRequest;
    }

}
