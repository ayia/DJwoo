package com.musichero.xmusic.playservice;

public interface IMusicFocusableListener {
    void onGainedAudioFocus();

    void onLostAudioFocus(boolean canDuck);
}
