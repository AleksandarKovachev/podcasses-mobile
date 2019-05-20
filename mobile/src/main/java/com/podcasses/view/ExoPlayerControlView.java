package com.podcasses.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.podcasses.R;

/**
 * Created by aleksandar.kovachev.
 */
public class ExoPlayerControlView extends PlayerControlView implements AdapterView.OnItemSelectedListener {

    private Spinner playerSpeed;
    private String[] speeds;

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

    public ExoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr, AttributeSet playbackAttrs) {
        super(context, attrs, defStyleAttr, playbackAttrs);
        init();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (getPlayer() != null) {
            getPlayer().setPlaybackParameters(new PlaybackParameters(Float.valueOf(speeds[position])));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void init() {
        playerSpeed = findViewById(R.id.player_speed);
        if (playerSpeed != null) {
            playerSpeed.setOnItemSelectedListener(this);
        }
        speeds = getResources().getStringArray(R.array.speed_values);
    }

}
