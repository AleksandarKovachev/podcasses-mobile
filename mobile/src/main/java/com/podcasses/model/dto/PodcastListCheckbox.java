package com.podcasses.model.dto;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.podcasses.BR;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastListCheckbox extends BaseObservable {

    private String name;

    private long id;

    private boolean isChecked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Bindable
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        notifyPropertyChanged(BR.checked);
    }
}
