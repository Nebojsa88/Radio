package com.radanov.audioplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.radanov.audioplayer.MainActivity;
import com.radanov.audioplayer.R;
import com.radanov.audioplayer.adapters.MusicAdapter;
import com.radanov.audioplayer.model.RadioStation;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MyService extends Service {

    private final ArrayList<RadioStation> radioList = new ArrayList<>();
    private StreamMediaPlayer mediaPlayer = StreamMediaPlayer.getInstance();
    private boolean prepared = false;
    private boolean started = false;
    private String filePath;
    private Context context;
    private String url;
    private String radioName;
    int position;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        if(radioList.size() < 1){
            inputRadioStations();
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        started = intent.getBooleanExtra("isStarted", false);

        filePath = intent.getStringExtra("filePath");
        url = intent.getStringExtra("url");
        position = intent.getIntExtra("position", 0);

        radioName = intent.getStringExtra("positionName");

        sendMessageToActivity(radioName);

        prepareMediaPlayerPosition();

        /*if (started) {
            started = false;
            mediaPlayer.pause();

        } else {
            started = true;
            mediaPlayer.start();
        }*/

        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification = new NotificationCompat.Builder(this, "ChannelID1")
                .setContentTitle("Audio Player")
                .setContentText("Radio is running")
                .setSmallIcon(R.drawable.notifications_icon)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        return START_STICKY;
    }

    public void prepareMediaPlayerPosition() {
        if(radioList.size() < 1){
            inputRadioStations();
        }
        mediaPlayer = StreamMediaPlayer.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }else {

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
            mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(radioList.get(MusicAdapter.TEST_POSITION).getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }
    public void prepareMediaPlayerNext() {
        if(radioList.size() < 1){
            inputRadioStations();
        }
        mediaPlayer = StreamMediaPlayer.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        else{
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        mediaPlayer.reset();

        if (MusicAdapter.TEST_POSITION == radioList.size() - 1) {
            MusicAdapter.TEST_POSITION = -1;
        }
        MusicAdapter.TEST_POSITION++;
        String testName = radioList.get(MusicAdapter.TEST_POSITION).getName();
        sendMessageToActivity(testName);
        //prepareMediaPlayerPrevious();
        try {

            mediaPlayer.setDataSource(radioList.get(MusicAdapter.TEST_POSITION).getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });

            //buttonPlayPause.setBackgroundResource(R.drawable.pause);

            //String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
            //textViewFileNameMusic.setText(newTitle);

            //textViewFileNameMusic.clearAnimation();
            //textViewFileNameMusic.startAnimation(animation);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void prepareMediaPlayerPrevious() {
        if(radioList.size() < 1){
            inputRadioStations();
        }
        mediaPlayer = StreamMediaPlayer.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }else {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        mediaPlayer.reset();

        if (MusicAdapter.TEST_POSITION == 0) {
            MusicAdapter.TEST_POSITION = radioList.size();
        }
        MusicAdapter.TEST_POSITION--;
        String testName = radioList.get(MusicAdapter.TEST_POSITION).getName();
        sendMessageToActivity(testName);

        try {

            mediaPlayer.setDataSource(radioList.get(MusicAdapter.TEST_POSITION).getUrl());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
            //buttonPlayPause.setBackgroundResource(R.drawable.pause);

            //String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
            //textViewFileNameMusic.setText(newTitle);

            //textViewFileNameMusic.clearAnimation();
            //textViewFileNameMusic.startAnimation(animation);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    "ChannelID1", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

    }

    public void prepareMediaPlayerPreviousNext() {
        //mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        position = MusicAdapter.TEST_POSITION;

        String previousUrl = radioList.get(position).getUrl();

        try {
            mediaPlayer.setDataSource(previousUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();

            //buttonPlayPause.setBackgroundResource(R.drawable.pause);

            //String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
            //textViewFileNameMusic.setText(newTitle);

            //textViewFileNameMusic.clearAnimation();
            //textViewFileNameMusic.startAnimation(animation);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToActivity(String msg) {
        Intent intent = new Intent("send_radio_name");
        // You can also include some extra data.
        intent.putExtra("radioName", msg);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /*private void connectUrlInBackground() {
        Observable.fromCallable(() -> {
            //mediaPlayer.setDataSource(STREAM_URL);
            mediaPlayer.prepare();

            return prepared;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(aBoolean -> Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show())
                .subscribe(aBoolean -> {
                    prepared = true;
                    *//*if (started) {
                        started = false;
                        mediaPlayer.pause();
                        //btnLiveMusic.setText("PLAY");

                    } else {
                        started = true;
                        mediaPlayer.start();
                        //btnLiveMusic.setText("Pause");
                    }*//*
                    //btnLiveMusic.setEnabled(true);
                    //btnLiveMusic.setText("Play");
                }, throwable -> Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show());
    }*/
    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    public void prepareMediaPlayer() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        else
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build());
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            prepared = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void inputRadioStations() {

        RadioStation radio1 = new RadioStation("Naxi Radio", "https://naxi128.streaming.rs:9152/;*.mp3");
        RadioStation radio2 = new RadioStation("Rock Radio", "https://mastermedia.shoutca.st/proxy/rockradio?mp=/stream");
        RadioStation radio3 = new RadioStation("Play Radio", "https://stream.playradio.rs:8443/play.mp3");
        RadioStation radio4 = new RadioStation("Delta Radio", "https://radio.dukahosting.com:7015/;*.mp3");
        RadioStation radio5 = new RadioStation("As", "https://mastermedia.shoutca.st/proxy/radioasfm?mp=/stream");
        RadioStation radio6 = new RadioStation("Fruska Gora", "https://player.iradio.pro/radiofruskagora/");
        RadioStation radio7 = new RadioStation("Pingvin", "https://uzivo.radiopingvin.com/domaci1");
        RadioStation radio8 = new RadioStation("Radio Zelengrad", "https://usa5.fastcast4u.com/proxy/pddonlcc?mp=/1");
        RadioStation radio9 = new RadioStation("TDI", "https://streaming.tdiradio.com/tdiradio.mp3");
        RadioStation radio10 = new RadioStation("Ok Radio", "https://sslstream.okradio.net/;*.mp3");
        RadioStation radio11 = new RadioStation("Radio JAT", "https://streaming.radiojat.rs/radiojat.mp3");
        RadioStation radio12 = new RadioStation("Cool", "https://live.coolradio.rs/cool128");
        RadioStation radio13 = new RadioStation("Radio In", "https://radio3-64ssl.streaming.rs:9212/;*.mp3");

        radioList.add(radio1);
        radioList.add(radio2);
        radioList.add(radio3);
        radioList.add(radio4);
        radioList.add(radio5);
        radioList.add(radio6);
        radioList.add(radio7);
        radioList.add(radio8);
        radioList.add(radio9);
        radioList.add(radio10);
        radioList.add(radio11);
        radioList.add(radio12);
        radioList.add(radio13);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
