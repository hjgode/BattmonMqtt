package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.net.ConnectivityManagerCompat;

public class MyNetwork {

    /**
     * source: https://github.com/evernote/android-job/blob/master/library/src/main/java/com/evernote/android/job/util/Device.java
     *
     * Checks the network condition of the device and returns the best type. If the device
     * is connected to a WiFi and mobile network at the same time, then it would assume
     * that the connection is unmetered because of the WiFi connection.
     *
     * @param context Any context, e.g. the application context.
     * @return The current network type of the device.
     */
    @NonNull
    @SuppressWarnings("deprecation")
    public static NetworkType getNetworkType(@NonNull Context context) {
        util.LOG("getting NetworkInfo...");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
//        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        try {
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Throwable t) {
            util.LOG("getting NetworkInfo...crashed: "+t.getMessage());
            return NetworkType.ANY;
        }


        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            util.LOG("getting NetworkInfo...no NetworkInfor or not connected");
            return NetworkType.ANY;
        }else{
            if(networkInfo!=null) {
                util.LOG("getting NetworkInfo...UMETERED");
                return NetworkType.UNMETERED;
            }
            else {
                util.LOG("getting NetworkInfo...not connected");
                return NetworkType.ANY; //null or not connected
            }
        }

/*
        boolean metered = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager);
        if (!metered) {
            util.LOG("getting NetworkInfo...UNMETERED");
            return NetworkType.UNMETERED;
        }else{
            if(ConnectivityManager.EXTRA_NETWORK_TYPE=="WIFI")
                return NetworkType.UNMETERED;
            util.LOG("getting NetworkInfo...METERED");
            return NetworkType.METERED;
        }

        if (isRoaming(connectivityManager, networkInfo)) {
            util.LOG("getting NetworkInfo...CONNECTED");
            return NetworkType.CONNECTED;
        } else {
            util.LOG("getting NetworkInfo...NOT_ROAMING");
            return NetworkType.NOT_ROAMING;
        }
*/
    }

    @SuppressWarnings("deprecation")
    private static boolean isRoaming(ConnectivityManager connectivityManager, NetworkInfo networkInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return networkInfo.isRoaming();
        }

        try {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
        } catch (Exception e) {
            return networkInfo.isRoaming();
        }
    }

    public enum NetworkType {
        /**
         * Network does not have to be connected.
         */
        ANY,
        /**
         * Network must be connected.
         */
        CONNECTED,
        /**
         * Network must be connected and unmetered.
         */
        UNMETERED,
        /**
         * Network must be connected and not roaming, but can be metered.
         */
        NOT_ROAMING,
        /**
         * This job requires metered connectivity such as most cellular data networks.
         */
        METERED
    }
}
