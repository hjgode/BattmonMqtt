package com.hjgode.BattmonMqtt;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.preference.EditTextPreference;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
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


    public static class SettingsFragment extends PreferenceFragmentCompat  {

        @Override
        public void onCreate(Bundle SavedInstance){
            super.onCreate(SavedInstance);
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            final SharedPreferences prefs=getContext().getSharedPreferences("gameSettings",0);
            final SharedPreferences.Editor prefsedit=prefs.edit();
            EditTextPreference port= (EditTextPreference) findPreference("mqtt_port");
            port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int p = Integer.parseInt(newValue.toString());
                        prefsedit.putInt(getResources().getString(R.string.mqtt_port), p);
                        prefsedit.apply();

                        Toast.makeText(getContext(), newValue + " set as new port", Toast.LENGTH_LONG);
                        return true;
                    }catch(Exception ex){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("MQTT port");
                        builder.setMessage("Only a number between 1 and 16484 is allowed");
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(getContext(), newValue + " set not a valid port", Toast.LENGTH_LONG).show();
                                    }
                                });//builder
                        builder.show();
//                        Toast.makeText(getContext(),newValue+" is not a valid port",Toast.LENGTH_LONG);
                        return false;
                    }
                }
            });

        }
    }
}