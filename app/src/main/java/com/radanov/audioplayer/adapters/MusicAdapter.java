package com.radanov.audioplayer.adapters;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.radanov.audioplayer.MainActivity;
import com.radanov.audioplayer.R;
import com.radanov.audioplayer.model.RadioStation;
import com.radanov.audioplayer.service.MyService;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{

    private ArrayList<RadioStation> radioStations;
    public static Context mContext;
    private String radioName;
    public static int TEST_POSITION;
    MyService myService;

    public MusicAdapter(ArrayList<RadioStation> list, Context mContext) {
        this.radioStations = list;
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_music, parent, false);

        return new MusicViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.MusicViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.textViewFileName.setText(radioStations.get(position).getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isClickable) {
                    View root = v.getRootView();
                    TextView name = root.findViewById(R.id.textRadioName);
                    name.setText(radioStations.get(position).getName());

                    TEST_POSITION = position;
                    myService = new MyService();
                    if (isMyServiceRunning(MyService.class)) {
                        myService.prepareMediaPlayerPosition();
                    } else {
                        startForegroundService(position);
                    }
                }else{
                    Toast.makeText(mContext, "Please check your internet !", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*String filePath = list.get(position);
        Log.e("filepath: " , filePath);

        String title = filePath.substring(filePath.lastIndexOf("/")+ 1);

        holder.textViewFileName.setText(title);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MusicActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("filepath", filePath);
                intent.putExtra("position", position);
                intent.putExtra("list", list);

                mContext.startActivity(intent);
            }
        });*/
    }
    private void startForegroundService(int position) {
        Intent serviceIntent = new Intent(mContext, MyService.class);
        String urlPosition;
        urlPosition = radioStations.get(position).getUrl();
        radioName = radioStations.get(position).getName();

        serviceIntent.putExtra("url", urlPosition);
        serviceIntent.putExtra("positionName", radioName);
        serviceIntent.putExtra("position", position);
        ContextCompat.startForegroundService(mContext, serviceIntent);
    }

    @Override
    public int getItemCount() {
        return radioStations.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewFileName;
        private CardView cardView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFileName = itemView.findViewById(R.id.text_viewCard);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
