package com.radanov.audioplayer.adapters;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.radanov.audioplayer.R;
import com.radanov.audioplayer.model.RadioStation;
import com.radanov.audioplayer.service.MyService;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{

    private ArrayList<RadioStation> radioStations;
    private Context mContext;
    private String radioName;
    public static int TEST_POSITION;
    private boolean serviceStarted;

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
    public void onBindViewHolder(@NonNull MusicAdapter.MusicViewHolder holder, int position) {

        holder.textViewFileName.setText(radioStations.get(position).getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View root = v.getRootView();
                    TextView name = root.findViewById(R.id.textRadioName);
                    name.setText(radioStations.get(position).getName());

                TEST_POSITION = position;
                MyService myService = new MyService();
                if (isMyServiceRunning(MyService.class)){

                    myService.prepareMediaPlayerPosition();
                    /*Intent intent = new Intent("send_radio_name");
                    // You can also include some extra data.
                    intent.putExtra("radioPosition", position);

                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);*/
                    serviceStarted = false;
                }else {
                    startForegroundService(position);
                    serviceStarted = true;
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
