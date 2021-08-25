package com.radanov.audioplayer.utils;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtils {

    public static boolean hasInternetAccess(ConnectivityManager cm) {
        boolean hasInternet = false;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            hasInternet = networkInfo.isConnectedOrConnecting();
            if(hasInternet) {
                // check is WIFI
                hasInternet = networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return hasInternet;
    }

    public static boolean hasNetwork(ConnectivityManager cm) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        else {
            return hasInternetAccess(cm);
        }
    }

}
