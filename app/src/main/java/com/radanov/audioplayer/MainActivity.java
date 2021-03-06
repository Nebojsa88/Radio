package com.radanov.audioplayer;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.radanov.audioplayer.adapters.MusicAdapter;
import com.radanov.audioplayer.model.RadioStation;
import com.radanov.audioplayer.service.MyService;
import com.radanov.audioplayer.utils.NetworkUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private int position;
    public static boolean isClickable = true;
    private TextView textRadioName;
    private String radioName;
    private Button btnLiveMusic;
    private Button btnPrevious;
    private Button btnNext;
    private RecyclerView recyclerView;
    //private final static String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";
    private final ArrayList<RadioStation> radioList = new ArrayList<>();
    private MusicAdapter adapter;
    private TextView textInternet;
    private MyService myService = new MyService();

    private boolean connected;
    private ConnectivityManager connectivityManager;


    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (connected)
                return;
            Bundle extras = intent.getExtras();
            NetworkInfo networkInfo = null;
            if (extras != null) {
                networkInfo = extras.getParcelable("networkInfo");
            }
            NetworkInfo.State state = null;
            if (networkInfo != null) {
                state = networkInfo.getState();
            }
            if (state == NetworkInfo.State.CONNECTED) {
                textInternet.setVisibility(View.GONE);

                btnNext.setEnabled(true);
                btnPrevious.setEnabled(true);
                btnLiveMusic.setEnabled(true);
                isClickable = true;

            } else {
                noInternet();
            }
        }
    };

    private void noInternet() {
        if (!NetworkUtils.hasNetwork(connectivityManager)) {
            textInternet.setVisibility(View.VISIBLE);
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(false);
            btnLiveMusic.setEnabled(false);
            isClickable = false;
            if(isMyServiceRunning(MyService.class)){
                stopService();
                textRadioName.setText("");
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Radio");

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        inputRadioStations();
        checkTheme();
        textRadioName = findViewById(R.id.textRadioName);

        textInternet = findViewById(R.id.textInternet);

        btnLiveMusic = (Button) findViewById(R.id.btnLiveMusic);
        btnPrevious = (Button) findViewById(R.id.buttonPrevious);
        btnNext = (Button) findViewById(R.id.buttonNext);

       if(isMyServiceRunning(MyService.class)){

           textRadioName.setText(radioList.get(MusicAdapter.TEST_POSITION).getName());
       }
        if (!haveNetworkConnection() && isMyServiceRunning(MyService.class)){
            stopService();
            textRadioName.setText("");

        }

        setRecyclerView();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("send_radio_name"));


        if (isMyServiceRunning(MyService.class)){

            btnLiveMusic.setEnabled(true);
            btnLiveMusic.setText("Pause");
        }else{
            btnLiveMusic.setText("Play");
        }
        btnLiveMusic.setText("Stop");

        isClickable = true;

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMyServiceRunning(MyService.class)){
                    Toast.makeText(MainActivity.this, "Please select radio station from list !", Toast.LENGTH_SHORT).show();

                }else{
                    if (haveNetworkConnection() && isMyServiceRunning(MyService.class)){
                        position = MusicAdapter.TEST_POSITION;
                        myService.prepareMediaPlayerPrevious();
                    }else{
                        btnPrevious.setEnabled(false);
                        Toast.makeText(MainActivity.this, "Please check your internet !", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isMyServiceRunning(MyService.class)){
                    Toast.makeText(MainActivity.this, "Please select radio station from list !", Toast.LENGTH_SHORT).show();

                }else {
                    if (haveNetworkConnection()){
                        position = MusicAdapter.TEST_POSITION;
                        myService.prepareMediaPlayerNext();
                    }else{
                        btnNext.setEnabled(false);
                        Toast.makeText(MainActivity.this, "Please check your internet !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnLiveMusic.setOnClickListener(v -> {

            if (isMyServiceRunning(MyService.class)) {
                stopService();
                textRadioName.setText("");
                //btnLiveMusic.setText("Play");

            } /*else {
                startService();
                btnLiveMusic.setText("Pause");
            }*/
            /*if (started) {
                    started = false;
                    mediaPlayer.pause();
                    btnLiveMusic.setText("PLAY");

                } else {
                    started = true;
                    mediaPlayer.start();
                    btnLiveMusic.setText("Pause");
                }*/
        });
        //Log.e("Media path", MEDIA_PATH);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        } /*else {
            //getAllAudioFiles();
        }*/
    }
    private void getAllAudioFilesTest() {
        if (Environment.isExternalStorageEmulated()) {

        } else {

        }
    }


    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            radioName = intent.getStringExtra("radioName");
            position = intent.getIntExtra("radioPosition", 0);

            textRadioName.setText(radioName);
        }
    };

    public void setRecyclerView(){

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MusicAdapter(radioList, MainActivity.this);
        recyclerView.setAdapter(adapter);

    }
    /*private void getAllAudioFiles() {

        if (MEDIA_PATH != null) {
            File mainFile = new File(MEDIA_PATH);
            File[] fileList = mainFile.listFiles();

            for (File file : fileList) {
                // Log.e("Media path", file.toString());

                if (file.isDirectory()) {

                    scanDirectory(file);
                } else {
                    String path = file.getAbsolutePath();

                    if (path.endsWith(".mp3")) {
                        songList.add(path);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
        adapter = new MusicAdapter(songList, MainActivity.this);
        recyclerView.setAd
        apter(adapter);
    }*/

    /*private void scanDirectory(File directory) {

        if (directory != null) {
            File[] fileList = directory.listFiles();
            for (File file : fileList) {

                if (file.isDirectory()) {
                    Log.e("Media path", file.toString());
                    // scanDirectory(file);
                } else {

                    String path = file.getAbsolutePath();
                    if (path.endsWith(".mp3")) {
                        songList.add(path);
                    }
                }
            }
        }
    }*/
    public void inputRadioStations(){

        RadioStation radio1 = new RadioStation("Naxi Radio", "https://naxi128.streaming.rs:9152/;*.mp3");
        RadioStation radio2 = new RadioStation("Rock Radio", "https://mastermedia.shoutca.st/proxy/rockradio?mp=/stream");
        RadioStation radio3 = new RadioStation("Play Radio", "https://stream.playradio.rs:8443/play-low.aac");
        RadioStation radio4 = new RadioStation("Delta Radio", "https://radio.dukahosting.com:7015/;*.mp3");
        RadioStation radio5 = new RadioStation("As", "https://mastermedia.shoutca.st/proxy/radioasfm?mp=/stream");
        RadioStation radio6 = new RadioStation("Fruska Gora", "https://player.iradio.pro/radiofruskagora/");
        RadioStation radio7 = new RadioStation("Pingvin", "https://uzivo.radiopingvin.com/domaci1");
        RadioStation radio8 = new RadioStation("Radio Zelengrad", "https://usa5.fastcast4u.com/proxy/pddonlcc?mp=/1");
        RadioStation radio9 = new RadioStation("TDI", "https://streaming.tdiradio.com/tdiradionovisad.aac");
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
    public void stopService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);
    }
    public void startService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    /*private void connectUrlInBackground() {
        Observable.fromCallable(() -> {
            mediaPlayer.setDataSource(streamUrl);
            mediaPlayer.prepare();

            return prepared;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(aBoolean -> Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show())
        .subscribe(aBoolean -> {
            prepared = true;
            btnLiveMusic.setEnabled(true);
            btnLiveMusic.setText("Play");
        }, throwable -> Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());
        }*/

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void checkTheme(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /*if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //getAllAudioFiles();

        }*/
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

    @Override
    protected void onPause() {
        if (!haveNetworkConnection() && isMyServiceRunning(MyService.class)){
            stopService();
        }
        super.onPause();

    }
    public void refreshActivity(){
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);

    }
    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, intentFilter);
        if (!haveNetworkConnection()) {
            if (isMyServiceRunning(MyService.class)){
                stopService();
            }
            connected = false;

            Toast.makeText(this, "Please check internet connection", Toast.LENGTH_SHORT).show();

        }
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        stopService();
        unregisterReceiver(connectivityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();

    }
}