package com.hjgode.BattmonMqtt;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

public class util {
    public static void LOG(String m){
        Log.d("WORKER",m);
    }
    public static String dumpPrefs(SharedPreferences sharedPreferences){
        StringBuilder stringBuilder=new StringBuilder();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            stringBuilder.append(entry.getKey() + ": " + entry.getValue().toString()+", ");
        }
        return stringBuilder.toString();
    }

}
