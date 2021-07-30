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
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.radanov.audioplayer.MainActivity;
import com.radanov.audioplayer.R;
import com.radanov.audioplayer.adapters.MusicAdapter;
import com.radanov.audioplayer.model.RadioStation;

import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MyService extends Service {

    private final ArrayList<RadioStation> radioList = new ArrayList<>();
    //private String filePath;
    public static Context context;
    private String radioName;
    int position;
    private StreamExoPlayer exoPlayer;

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

        //filePath = intent.getStringExtra("filePath");
        radioName = intent.getStringExtra("positionName");

        sendMessageToActivity(radioName);

        prepareMediaPlayerPosition();

        Intent intent1 = new Intent(this, MainActivity.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN ){
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

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

        trustManager();
        if(radioList.size() < 1){
            inputRadioStations();
        }
        exoPlayer = StreamExoPlayer.getInstance(MusicAdapter.mContext);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MusicAdapter.mContext,
                Util.getUserAgent(MusicAdapter.mContext, "Audio Player"));

        MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(radioList.get(MusicAdapter.TEST_POSITION).getUrl()));

        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);
        /*mediaPlayer = StreamMediaPlayer.getInstance();

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
        }*/

    }
    public void prepareMediaPlayerNext() {
        if(radioList.size() < 1){
            inputRadioStations();
        }
        /*mediaPlayer = StreamMediaPlayer.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        else{
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        mediaPlayer.reset();*/
        if (MusicAdapter.TEST_POSITION == radioList.size() - 1) {
            MusicAdapter.TEST_POSITION = -1;
        }
        MusicAdapter.TEST_POSITION++;
        String testName = radioList.get(MusicAdapter.TEST_POSITION).getName();
        sendMessageToActivity(testName);

        exoPlayer = StreamExoPlayer.getInstance(MusicAdapter.mContext);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MusicAdapter.mContext,
                Util.getUserAgent(MusicAdapter.mContext, "Audio Player"));

        MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(radioList.get(MusicAdapter.TEST_POSITION).getUrl()));

        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);

        //prepareMediaPlayerPrevious();
       /* try {

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
        }*/

    }
    public void prepareMediaPlayerPrevious() {
        if(radioList.size() < 1){
            inputRadioStations();
        }
        /*mediaPlayer = StreamMediaPlayer.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }else {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        mediaPlayer.reset();*/

        if (MusicAdapter.TEST_POSITION == 0) {
            MusicAdapter.TEST_POSITION = radioList.size();
        }
        MusicAdapter.TEST_POSITION--;
        String testName = radioList.get(MusicAdapter.TEST_POSITION).getName();
        sendMessageToActivity(testName);

        exoPlayer = StreamExoPlayer.getInstance(MusicAdapter.mContext);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MusicAdapter.mContext,
                Util.getUserAgent(MusicAdapter.mContext, "Audio Player"));

        MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(radioList.get(MusicAdapter.TEST_POSITION).getUrl()));

        exoPlayer.prepare(audioSource);
        exoPlayer.setPlayWhenReady(true);
        /*try {

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
        }*/
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    "ChannelID1", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
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
        exoPlayer.stop();
        exoPlayer.release();
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    public void trustManager(){
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        // Try "SSL" or Replace with "TLS"
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
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
        RadioStation radio14 = new RadioStation("Radio S", "https://stream.radios.rs:9000/;*.mp3");
        RadioStation radio15 = new RadioStation("Nostalgija", "https://nostalgie64ssl.streaming.rs:9262/;*.mp3");
        RadioStation radio16 = new RadioStation("Karolina", "https://streaming.karolina.rs/karolina.mp3");
        RadioStation radio17 = new RadioStation("Moj Radio", "https://eu4.fastcast4u.com/proxy/svidakov?mp=/1");
        RadioStation radio18 = new RadioStation("Hit Radio", "https://streaming.hitfm.rs/hit.mp3");
        RadioStation radio19 = new RadioStation("Laguna", "https://live.radiolaguna.rs/laguna");
        RadioStation radio20 = new RadioStation("Top Fm", "https://topfm64ssl.streaming.rs:9287/;*.mp3");
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
        radioList.add(radio14);
        radioList.add(radio15);
        radioList.add(radio16);
        radioList.add(radio17);
        radioList.add(radio18);
        radioList.add(radio19);
        radioList.add(radio20);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
