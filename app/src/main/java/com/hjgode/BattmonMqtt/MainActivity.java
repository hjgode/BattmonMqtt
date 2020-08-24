package com.hjgode.BattmonMqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

  static MainActivity mainActivity;
  TextView txtBattInfo, log_text;
  BroadcastReceiver receiver;

  private Switch switchButton;
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
    txtBattInfo.setMovementMethod(new ScrollingMovementMethod());
    log_text=findViewById(R.id.log_text);
    log_text.setMovementMethod(new ScrollingMovementMethod());

    //restore content
    if (savedInstanceState != null) {
      // Restore value of members from saved state
      String s = savedInstanceState.getString(Constants.LOG_TEXT);
      log_text.setText(s);
    } else {
      // Probably initialize members with default values for a new instance

    }

    SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
    String device=sharedPreferences.getString(getResources().getString(R.string.mqtt_topic),
            getResources().getString(R.string.mqtt_default_topic));
    if(device.equals(getResources().getString(R.string.mqtt_default_topic))) {
      util.LOG("changing default device");
      //ensure to use an Editor instance to change the value!
      SharedPreferences.Editor editor=sharedPreferences.edit();
      device = Build.DEVICE;
      editor.putString(getResources().getString(R.string.mqtt_topic),device);
//      editor.putString(getResources().getString(R.string.mqtt_default_topic),device);
      editor.apply();
      if(editor.commit()){
        util.LOG("Changed default device to "+device+" and persisted");
      }
      util.LOG("Changed default device");
      util.dumpPrefs(sharedPreferences);
    }

    //show messages from background threads
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String s = intent.getStringExtra(Constants.SERVICE_MESSAGE);
        if(s.equals("CLEAR"))
          log_text.setText("");
        else
          log_text.append(s+"\n");
      }
    };
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
    LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(Constants.RESULT_INTENT));
  }

  @Override
  protected void onSaveInstanceState(Bundle bundle){
    super.onSaveInstanceState(bundle);
    String s=bundle.getString(Constants.LOG_TEXT);
    log_text.setText(s);
  }

  //see onCreate
//  @Override
//  protected void onRestoreInstanceState(Bundle savedInstanceState) {
//    super.onRestoreInstanceState(savedInstanceState);
//    String s=savedInstanceState.getString(Constants.LOG_TEXT);
//    log_text.setText(s);
//  }

  @Override
  protected void onPause(){
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    super.onPause();
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
    switchButton = findViewById(R.id.notification_switch);
    switchButton.setTextOff("foreground service is off");
    switchButton.setTextOn("foreground service is on and periodic work is running");
    switchButton.setChecked(NotificationConfig.isNotificationOn());
    switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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