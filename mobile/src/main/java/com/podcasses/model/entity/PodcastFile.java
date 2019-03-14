package com.podcasses.model.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastFile {

    private String id;

    private String userId;

    private String fileName;

    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPodcastFileDate() {
        DateFormat formatter = SimpleDateFormat.getDateInstance();
        return formatter.format(getDate());
    }

}
