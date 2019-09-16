package com.podcasses.model.entity.base;

import android.text.Spanned;

import androidx.core.text.HtmlCompat;
import androidx.databinding.Bindable;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.podcasses.BR;
import com.podcasses.database.DateConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by aleksandar.kovachev.
 */
public class Podcast extends BaseLikeModel {

    @PrimaryKey(autoGenerate = true)
    private int internalId;

    private String id;

    private String channelId;

    private String channel;

    private String channelImageUrl;

    private String guid;

    private String title;

    private String description;

    private int isActive;

    private Integer categoryId;

    private Integer privacyId;

    private boolean hasComments;

    private Integer languageId;

    private int views;

    private int downloads;

    private String duration;

    private String podcastFileName;

    private String imageFileName;

    private String podcastUrl;

    private String imageUrl;

    private String downloadUrl;

    private String userId;

    private boolean markAsPlayed = false;

    @TypeConverters({DateConverter.class})
    private Date createdTimestamp;

    @TypeConverters({DateConverter.class})
    private Date updatedTimestamp;

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelImageUrl() {
        return channelImageUrl;
    }

    public void setChannelImageUrl(String channelImageUrl) {
        this.channelImageUrl = channelImageUrl;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getPrivacyId() {
        return privacyId;
    }

    public void setPrivacyId(Integer privacyId) {
        this.privacyId = privacyId;
    }

    public boolean isHasComments() {
        return hasComments;
    }

    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
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


    public String getPodcastUrl() {
        return podcastUrl;
    }

    public void setPodcastUrl(String podcastUrl) {
        this.podcastUrl = podcastUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
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
        return HtmlCompat.fromHtml(description.replaceAll("\\\n", "<br/>"), 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Podcast)) return false;
        Podcast podcast = (Podcast) o;
        return getId().equals(podcast.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Podcast() {
    }

    public Podcast(Podcast podcast) {
        internalId = podcast.internalId;
        id = podcast.id;
        channel = podcast.channel;
        channelId = podcast.channelId;
        channelImageUrl = podcast.channelImageUrl;
        guid = podcast.guid;
        title = podcast.title;
        description = podcast.description;
        isActive = podcast.isActive;
        categoryId = podcast.categoryId;
        privacyId = podcast.privacyId;
        hasComments = podcast.hasComments;
        languageId = podcast.languageId;
        views = podcast.views;
        downloads = podcast.downloads;
        duration = podcast.duration;
        podcastFileName = podcast.podcastFileName;
        imageFileName = podcast.imageFileName;
        podcastUrl = podcast.podcastUrl;
        imageUrl = podcast.imageUrl;
        downloadUrl = podcast.downloadUrl;
        userId = podcast.userId;
        markAsPlayed = podcast.markAsPlayed;
        createdTimestamp = podcast.createdTimestamp;
        updatedTimestamp = podcast.updatedTimestamp;
    }

}
