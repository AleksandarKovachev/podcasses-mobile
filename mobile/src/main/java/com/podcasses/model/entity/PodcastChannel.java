package com.podcasses.model.entity;

import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.podcasses.database.DateConverter;

import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
public class PodcastChannel {

    @NonNull
    @PrimaryKey
    private String id;

    private String userId;

    private String title;

    private String author;

    private String description;

    private String rssFeedUrl;

    private String rssFeedGuid;

    private Integer verified;

    private String username;

    private String email;

    private String imageUrl;

    private String imageFileName;

    private int categoryId;

    private int languageId;

    private int isSubscribed;

    @TypeConverters({DateConverter.class})
    private Date createdTimestamp;

    @TypeConverters({DateConverter.class})
    private Date updatedTimestamp;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRssFeedUrl() {
        return rssFeedUrl;
    }

    public void setRssFeedUrl(String rssFeedUrl) {
        this.rssFeedUrl = rssFeedUrl;
    }

    public String getRssFeedGuid() {
        return rssFeedGuid;
    }

    public void setRssFeedGuid(String rssFeedGuid) {
        this.rssFeedGuid = rssFeedGuid;
    }

    public Integer getVerified() {
        return verified;
    }

    public void setVerified(Integer verified) {
        this.verified = verified;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public int getIsSubscribed() {
        return isSubscribed;
    }

    public void setIsSubscribed(int isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Date getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(Date updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public Spanned getPodcastChannelDescription() {
        return HtmlCompat.fromHtml(description.replaceAll("\\\n", "<br/>"), 0);
    }

}
