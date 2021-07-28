package com.radanov.audioplayer.service;

import android.content.Context;

import com.google.android.exoplayer2.SimpleExoPlayer;


public class StreamExoPlayer extends SimpleExoPlayer {

    private static StreamExoPlayer exoPlayer;

    private StreamExoPlayer(Builder builder) {
        super(builder);
    }

    public static StreamExoPlayer getInstance(Context context) {
        if(exoPlayer == null) {
            exoPlayer = new StreamExoPlayer(new SimpleExoPlayer.Builder(context));

        }
        return exoPlayer;
    }
    @Override
    public void release() {
        super.release();
        exoPlayer = null;
    }
}
