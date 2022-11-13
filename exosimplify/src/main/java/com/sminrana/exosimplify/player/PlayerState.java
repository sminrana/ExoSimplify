package com.sminrana.exosimplify.player;

public enum PlayerState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    COMPLETE;

    private PlayerState() {
    }
}
