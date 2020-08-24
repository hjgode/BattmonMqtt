package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

  static MainActivity mainActivity;
  TextView txtBattInfo;

  private ToggleButton toggleButton;
  String[] perms=new String[]{
          "android.permission.RECEIVE_BOOT_COMPLETED",
          "android.permission.INTERNET",
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mainActivity=this;

    txtBattInfo=findViewById(R.id.txtBattInfo);

    SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
    String device=sharedPreferences.getString(getResources().getString(R.string.mqtt_topic),
            getResources().getString(R.string.mqtt_default_topic));
    if(device==getResources().getString(R.string.mqtt_default_topic)) {
      device = Build.DEVICE;
      sharedPreferences.edit().putString(getResources().getString(R.string.mqtt_topic),device);
      sharedPreferences.edit().putString(getResources().getString(R.string.mqtt_default_topic),device);
      sharedPreferences.edit().apply();
      if(sharedPreferences.edit().commit()){
        util.LOG("Changed default device to "+device+" and persisted");
      }
    }

    initView();
    setWork();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      Intent intent = new Intent(MainActivity.this, BattmonMqttSettingsActivity.class);
      startActivity(intent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    BatteryInfo.BattInfo batteryInfo=BatteryInfo.getBattInfo(getApplicationContext());
    txtBattInfo.setText(batteryInfo.toString());
  }

  private void setWork() {
    util.LOG("setWork...");
    util.LOG("doWork: creating periodicWorkRequest...");
    SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
    util.LOG("setWork-Prefs="+util.dumpPrefs(sharedPreferences));
    String intervalS=sharedPreferences.getString(this.getResources().getString(R.string.mqtt_interval),
            this.getResources().getString(R.string.mqtt_default_interval));
    long interval=Long.parseLong(intervalS);

    //workmanager periodic work (not less than 15 minutes)
    PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
        .Builder(PeriodWorker.class, interval, TimeUnit.MINUTES)
        .build();
    WorkManager.getInstance().enqueueUniquePeriodicWork("pullwork", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
  }

  public static MainActivity getInstance(){
    return mainActivity;
  }

  public void startWorker(Context ctx, boolean isChecked) {
    Intent intent = new Intent(NotificationConfig.getContext(), ForeService.class);
    intent.setAction("android.intent.action.PULL_UP");
    if (isChecked) {
      NotificationConfig.updateNotificationStatus(true);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent);
      } else {
        startService(intent);
      }
    } else {
      NotificationConfig.updateNotificationStatus(false);
      stopService(intent);
    }
  }

  public void startWorker(Context ctx){
    startWorker(ctx, true);
  }

  private void initView() {
    toggleButton = findViewById(R.id.notification_switch);
    toggleButton.setTextOff("foreground service is off");
    toggleButton.setTextOn("foreground service is on and periodic work is running");
    toggleButton.setChecked(NotificationConfig.isNotificationOn());
    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        startWorker(getApplicationContext(), isChecked);
/*
        Intent intent = new Intent(NotificationConfig.getContext(), ForeService.class);
        intent.setAction("android.intent.action.PULL_UP");
        if (isChecked) {
          NotificationConfig.updateNotificationStatus(true);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
          } else {
            startService(intent);
          }
        } else {
          NotificationConfig.updateNotificationStatus(false);
          stopService(intent);
        }
*/
      }
    });
  }
}