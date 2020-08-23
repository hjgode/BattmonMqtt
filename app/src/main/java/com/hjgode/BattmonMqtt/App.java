package com.hjgode.BattmonMqtt;

import android.app.Application;

public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    NotificationConfig.setContext(getApplicationContext());
  }
}
