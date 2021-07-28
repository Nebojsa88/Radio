package com.radanov.audioplayer.service;

import android.media.MediaPlayer;

public class StreamMediaPlayer extends MediaPlayer {

    private static StreamMediaPlayer mediaPlayer;

    private StreamMediaPlayer() {
    }

    public static StreamMediaPlayer getInstance() {
        if(mediaPlayer == null) {
            mediaPlayer = new StreamMediaPlayer();
        }
        return mediaPlayer;
    }
    @Override
    public void release() {
        super.release();

        mediaPlayer = null;
    }
}
