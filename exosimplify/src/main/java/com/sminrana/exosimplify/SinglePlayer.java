package com.sminrana.exosimplify;

import com.google.android.exoplayer2.ExoPlayer;

public final class SinglePlayer {
    private static SinglePlayer instance = null;

    private SinglePlayer () {

    }

    public static synchronized SinglePlayer getInstance() {
        if (instance ==  null) {
            instance =  new SinglePlayer();
        }

        return instance;
    }

    public ExoPlayer player;
}
