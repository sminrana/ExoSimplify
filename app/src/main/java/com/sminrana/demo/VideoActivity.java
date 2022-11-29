package com.sminrana.demo;

import android.util.Log;

import com.sminrana.exosimplify.ui.SimplifyVideoActivity;

public class VideoActivity extends SimplifyVideoActivity {
    private static final String TAG = "VideoActivity";

    @Override
    public void onAppKill() {
        super.onAppKill();
    }

    /**
     * This gets called when player completes a video
     * It never gets called if {@link #loop()} is set to true
     */
    @Override
    public void ended() {
        Log.v(TAG, "playing ended");
    }

    /**
     * Get any error if player throws one
     */
    @Override
    public void error(String error) {

    }

    /**
     * Enable loop
     */
    @Override
    public boolean loop() {
        return true;
    }
}
