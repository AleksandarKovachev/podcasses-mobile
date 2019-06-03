package com.podcasses.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.manager.SharedPreferencesManager;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class ExoPlayerControlView extends PlayerControlView implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private List<String> speeds;
    private boolean trimSilence;
    private float playbackSpeed;
    private SharedPreferencesManager sharedPreferencesManager;

    public ExoPlayerControlView(Context context) {
        super(context);
    }

    public ExoPlayerControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        trimSilence = isChecked;
        sharedPreferencesManager.setTrimSilence(trimSilence);
        if (getPlayer() != null) {
            getPlayer().setPlaybackParameters(new PlaybackParameters(playbackSpeed, 1f, trimSilence));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        playbackSpeed = Float.valueOf(speeds.get(position));
        sharedPreferencesManager.setPlaybackSpeed(playbackSpeed);
        if (getPlayer() != null) {
            getPlayer().setPlaybackParameters(new PlaybackParameters(playbackSpeed, 1f, trimSilence));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void init() {
        sharedPreferencesManager = ((BaseApplication) getContext().getApplicationContext()).getSharedPreferencesManager();
        speeds = Arrays.asList(getResources().getStringArray(R.array.speed_values));

        playbackSpeed = sharedPreferencesManager.getPlaybackSpeed();
        trimSilence = sharedPreferencesManager.isTrimSilince();

        AppCompatSpinner playerSpeed = findViewById(R.id.player_speed);
        if (playerSpeed != null) {
            playerSpeed.setSelection(speeds.indexOf(String.valueOf(playbackSpeed)));
            playerSpeed.setOnItemSelectedListener(this);
        }
        AppCompatCheckBox trimSilenceCheckbox = findViewById(R.id.trim_silence);
        if (trimSilenceCheckbox != null) {
            trimSilenceCheckbox.setChecked(trimSilence);
            trimSilenceCheckbox.setOnCheckedChangeListener(this);
        }
    }

}
