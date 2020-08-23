package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

  static MainActivity mainActivity;

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
  }

  private void setWork() {
    util.LOG("setWork...");
    util.LOG("doWork: creating periodicWorkRequest...");
    //workmanager periodic work (not less than 15 minutes)
    PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
        .Builder(PeriodWorker.class, 16, TimeUnit.MINUTES)
        .build();
    WorkManager.getInstance().enqueueUniquePeriodicWork("pullwork", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
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