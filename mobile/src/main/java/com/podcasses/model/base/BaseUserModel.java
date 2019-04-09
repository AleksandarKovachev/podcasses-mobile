package com.podcasses.model.base;

import com.podcasses.BR;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by aleksandar.kovachev.
 */
public abstract class BaseUserModel extends BaseObservable {

    private String userId;

    private String username;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

}
