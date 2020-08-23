package com.hjgode.BattmonMqtt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;

public class BattmonMqttSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            String key = findPreference(preference.getKey()).toString();
            util.LOG( "SettingsFragment onSharedPreferenceChanged called with key="+key+", new value="+o.toString());

            if (preference != null) {

/*
                if (!(preference instanceof CheckBoxPreference)) {
                    String value = sharedPreferences.getString(preference.getKey(), "");
                    Log.i(TAG, "changed key/value: " + key+"/"+value);
                }
                if(key==pref.PREF_MQTT_HOST){
                    sharedPreferences.edit().putString(pref.PREF_MQTT_HOST, sharedPreferences.getString(preference.getKey(), "192.168.0.40"));
                }else if(key==pref.PREF_MQTT_INTERVAL){
                    sharedPreferences.edit().putString(pref.PREF_MQTT_INTERVAL, sharedPreferences.getString(preference.getKey(), "15"));
                }else if(key==pref.PREF_MQTT_PORT){
                    sharedPreferences.edit().putString(pref.PREF_MQTT_PORT, sharedPreferences.getString(preference.getKey(), "1883"));
                }else if(key==pref.PREF_MQTT_TOPIC){
                    sharedPreferences.edit().putString(pref.PREF_MQTT_TOPIC, sharedPreferences.getString(preference.getKey(), "geraet1"));
                }else if(key==pref.PREF_MQTT_ENABLED){
                    sharedPreferences.edit().putBoolean(pref.PREF_MQTT_ENABLED, sharedPreferences.getBoolean(preference.getKey(),true));
                }
                sharedPreferences.edit().apply();
*/
//                MySharedPreferences mySharedPreferences=new MySharedPreferences(getContext());
//                MainActivity.getInstance().updateUI("onSharedPreferenceChanged: " + mySharedPreferences.toString());
            }
            return true; //true=update and commit
        }
    }
}