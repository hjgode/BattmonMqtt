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
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        try {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        } catch (Throwable t) {
            return NetworkType.ANY;
        }

        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return NetworkType.ANY;
        }

        boolean metered = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager);
        if (!metered) {
            return NetworkType.UNMETERED;
        }

        if (isRoaming(connectivityManager, networkInfo)) {
            return NetworkType.CONNECTED;
        } else {
            return NetworkType.NOT_ROAMING;
        }
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
