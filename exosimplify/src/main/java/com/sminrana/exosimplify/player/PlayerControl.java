package com.sminrana.exosimplify.player;

public interface PlayerControl {
    void play();
    void pause();
    void seekTo(long position);
    void ended();
    void error(String error);
    boolean loop();
}
