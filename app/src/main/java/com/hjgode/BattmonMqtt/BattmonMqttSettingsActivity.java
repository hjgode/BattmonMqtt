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
            boolean validated=true;
            if (preference != null) {
                String key = findPreference(preference.getKey()).toString();
                String value=o.toString();
                util.LOG( "SettingsFragment onSharedPreferenceChanged called with key="+key+", new value="+value);
                if(key.equals(getResources().getString(R.string.mqtt_port))){
                    int test=0;
                    try{
                        Integer.parseInt(value);
                    }catch(Exception ex){
                        validated=false;
                    }
                    if(test<1 || test>65536){
                        validated=false;
                    }
                }
                else if(key.equals(getResources().getString(R.string.mqtt_interval))){
                    int test=0;
                    try{
                        Integer.parseInt(value);
                    }catch(Exception ex){
                        validated=false;
                    }
                    if(test<1 || test>65536){
                        validated=false;
                    }
                }
            }
            return validated; //true=update and commit
        }
    }
}