package com.radanov.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.radanov.audioplayer.databinding.ActivityMusicBinding;
import com.radanov.audioplayer.service.MyService;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class MusicActivity extends AppCompatActivity {

    private Button buttonPlayPause, buttonPrevious, buttonNext;
    private TextView textViewFileNameMusic, textViewProgress, textViewTotalTime;
    private SeekBar seekBarVolume, seekBarMusic;

    private String title, filePath;
    private int position;
    private ArrayList<String> list;

    private MediaPlayer mediaPlayer;

    Runnable runnable;
    Handler handler;
    int totalTime;

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        textViewFileNameMusic = findViewById(R.id.textViewFileNameMusic);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewTotalTime = findViewById(R.id.textViewTotalTime);
        seekBarMusic = findViewById(R.id.musicSeekBar);
        seekBarVolume = findViewById(R.id.volumeSeekBar);

        animation = AnimationUtils.loadAnimation(MusicActivity.this, R.anim.translate_animation);

        textViewFileNameMusic.setAnimation(animation);

        title = getIntent().getStringExtra("title");
        filePath = getIntent().getStringExtra("filepath");
        position = getIntent().getIntExtra("position", 0);
        list = getIntent().getStringArrayListExtra("list");

        textViewFileNameMusic.setText(title);

        mediaPlayer = new MediaPlayer();
        /*try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mediaPlayer.reset();

                if(position == 0){
                    position = list.size() - 1;
                }else{
                    position --;
                }

                String newFilePath = list.get(position);

                try {
                    mediaPlayer.setDataSource(newFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    buttonPlayPause.setBackgroundResource(R.drawable.pause);

                    String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
                    textViewFileNameMusic.setText(newTitle);

                    textViewFileNameMusic.clearAnimation();
                    textViewFileNameMusic.startAnimation(animation);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMyServiceRunning(MyService.class)) {
                    stopService();
                    buttonPlayPause.setBackgroundResource(R.drawable.play);

                } else {
                    startService();
                    buttonPlayPause.setBackgroundResource(R.drawable.pause);
                }


                /*if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                   buttonPlayPause.setBackgroundResource(R.drawable.play);
                }else{
                    mediaPlayer.start();
                    buttonPlayPause.setBackgroundResource(R.drawable.pause);
                }*/
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.reset();

                if(position == list.size() - 1){
                    position = 0;
                }else{
                    position ++;
                }

                String newFilePath = list.get(position);

                try {
                    mediaPlayer.setDataSource(newFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    buttonPlayPause.setBackgroundResource(R.drawable.pause);

                    String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
                    textViewFileNameMusic.setText(newTitle);

                    textViewFileNameMusic.clearAnimation();
                    textViewFileNameMusic.startAnimation(animation);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    seekBarVolume.setProgress(progress);
                    float volumeLevel = progress / 100f;
                    mediaPlayer.setVolume(volumeLevel, volumeLevel);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(fromUser){
                   mediaPlayer.seekTo(progress);
                   seekBarMusic.setProgress(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /*handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                totalTime = mediaPlayer.getDuration();
                seekBarMusic.setMax(totalTime);

                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBarMusic.setProgress(currentPosition);
                handler.postDelayed(runnable, 1000);

                String elapsedTime = createTimeLabel(currentPosition);
                String lastTime = createTimeLabel(totalTime);

                textViewProgress.setText(elapsedTime);
                textViewTotalTime.setText(lastTime);

                if(elapsedTime.equals(lastTime)){

                    mediaPlayer.reset();

                    if(position == list.size() - 1){
                        position = 0;
                    }else{
                        position ++;
                    }

                    String newFilePath = list.get(position);

                    try {
                        mediaPlayer.setDataSource(newFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        buttonPlayPause.setBackgroundResource(R.drawable.pause);

                        String newTitle = newFilePath.substring(newFilePath.lastIndexOf("/")+ 1);
                        textViewFileNameMusic.setText(newTitle);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        handler.post(runnable);*/
    }

    public void stopService() {

        // Boolean isStarted = started;
        Intent serviceIntent = new Intent(this, MyService.class);
        //serviceIntent.putExtra("isStarted", isStarted);
        stopService(serviceIntent);

    }

    public void startService() {

        //Boolean isStarted = started;
        Intent serviceIntent = new Intent(this, MyService.class);

        serviceIntent.putExtra("filePath", filePath);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public String createTimeLabel(int currentPosition){

        //1 second = 1000 millisecond

        String timeLabel;
        int minute, second;

        minute = currentPosition / 1000 / 60;
        second = currentPosition / 1000 % 60;

        if(second < 10 ){

            timeLabel = minute + ":0"+ second;
        }else{
            timeLabel = minute + ":"+ second;
        }
        return timeLabel;
    }
}