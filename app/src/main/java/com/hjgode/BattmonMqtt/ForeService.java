package com.hjgode.BattmonMqtt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

public class ForeService extends Service {

  private static final String CHANNEL_ID = "fore";
  private static final String CHANNEL_NAME = "test";
  private Context context=this;
  private Runnable runnable=new Runnable() {
    @Override
    public void run() {
      Mqtt.doPublish(context);
    }
  };

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * called by periodic worker, multiple calls possible
   * @param intent
   * @param flags
   * @param startId
   * @return
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    util.LOG("ForeService onStartCommand called...starting doPublish");
    //TODO do something useful, runs on UI thread!
    new Thread(runnable).start();

    return Service.START_NOT_STICKY; //START_NOT_STICKY=service is restarted if terminated, intent is null then
    //START_NOT_STICKY=Service is not restarted. Used for services which are periodically triggered anyway.
    //START_REDELIVER_INTENT=Similar to Service.START_STICKY but the original Intent is re-delivered to the onStartCommand method
    /*
    You can check if the service was restarted via the Intent.getFlags() method. START_FLAG_REDELIVERY
    (in case the service was started with Service.START_REDELIVER_INTENT) or START_FLAG_RETRY (in case
    the service was started with Service.START_STICKY) is passed.
    */
  }

  @Override
  public void onCreate() {
    util.LOG("ForeService onCreate called...");
    String title = "Battmon Mqtt";
    String body = "periodic publish running";
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(PRIORITY_HIGH)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
          CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
      notificationManager.createNotificationChannel(notificationChannel);
    }

    util.LOG("foreService: starting notification...");
    startForeground(10000, notificationBuilder.build());
  }


}
