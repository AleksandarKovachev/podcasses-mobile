package com.podcasses.model.entity;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.podcasses.BR;
import com.podcasses.database.DateConverter;
import com.podcasses.model.base.BaseLikeModel;

import org.parceler.Parcel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
@Entity
@Parcel(Parcel.Serialization.BEAN)
public class Podcast extends BaseLikeModel {

    @NonNull
    @PrimaryKey
    private String id;

    private String title;

    private String quote;

    private String participator;

    private String description;

    private int isActive;

    private int categoryId;

    private int privacyId;

    private boolean hasComments;

    private boolean canDownload;

    private boolean ageRestrict;

    private int languageId;

    private int views;

    private int downloads;

    private String duration;

    private String podcastFileName;

    private String imageFileName;

    private String userId;

    private String username;

    private boolean markAsPlayed;

    @TypeConverters({DateConverter.class})
    private Date createdTimestamp;

    @TypeConverters({DateConverter.class})
    private Date updatedTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getParticipator() {
        return participator;
    }

    public void setParticipator(String participator) {
        this.participator = participator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getPrivacyId() {
        return privacyId;
    }

    public void setPrivacyId(int privacyId) {
        this.privacyId = privacyId;
    }

    public boolean isHasComments() {
        return hasComments;
    }

    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    public boolean isCanDownload() {
        return canDownload;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }

    public boolean isAgeRestrict() {
        return ageRestrict;
    }

    public void setAgeRestrict(boolean ageRestrict) {
        this.ageRestrict = ageRestrict;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Bindable
    public String getPodcastFileName() {
        return podcastFileName;
    }

    public void setPodcastFileName(String podcastFileName) {
        this.podcastFileName = podcastFileName;
        notifyPropertyChanged(BR.podcastFileName);
    }

    @Bindable
    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
        notifyPropertyChanged(BR.imageFileName);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Bindable
    public boolean isMarkAsPlayed() {
        return markAsPlayed;
    }

    public void setMarkAsPlayed(boolean markAsPlayed) {
        this.markAsPlayed = markAsPlayed;
        notifyPropertyChanged(BR.markAsPlayed);
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

    public String getPodcastDate() {
        DateFormat formatter = SimpleDateFormat.getDateInstance();
        return formatter.format(getCreatedTimestamp());
    }

    public Spanned getPodcastDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(description);
        }
    }

}
