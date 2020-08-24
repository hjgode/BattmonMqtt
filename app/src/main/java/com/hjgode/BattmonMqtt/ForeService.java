package com.hjgode.BattmonMqtt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

public class ForeService extends Service {

  private static final String CHANNEL_ID = "fore";
  private static final String CHANNEL_NAME = "test";
  private static final int NOTIFICATION_ID=10000;

  private Context context=this;
  static LocalBroadcastManager broadcaster=null;
  NotificationCompat.Builder notificationBuilder=null;
  NotificationManager notificationManager=null;

  private Runnable runnable=new Runnable() {
    @Override
    public void run() {
      SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
      boolean enabled = sharedPreferences.getBoolean(context.getResources().getString(R.string.mqtt_enbled),true);
      MyNetwork.NetworkType networkType=MyNetwork.getNetworkType(context);
      //do not start publish if not enabled
      if(enabled){
        //do not publish if not connected to unmetered network
        if(networkType == MyNetwork.NetworkType.UNMETERED) {
          sendResult("CLEAR");
          sendResult("ForeService starting doPublish");
          Mqtt.doPublish(context);
        }else{
          util.LOG("Not connected to home network. No doPublish()");
          sendResult("Not connected to home network. No doPublish()");
        }
      }else{
        util.LOG("MQTT publish not enabled. No doPublish()");
        sendResult("MQTT publish not enabled. No doPublish()");
      }
    }
  };

  public void sendResult(String message) {
    if(broadcaster==null)
      broadcaster = LocalBroadcastManager.getInstance(this);
    Intent intent = new Intent(Constants.RESULT_INTENT);
    if(message != null)
      intent.putExtra(Constants.SERVICE_MESSAGE, message);
    broadcaster.sendBroadcast(intent);
  }

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

    //to launch the activity when user taps notification
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,
            notificationIntent, PendingIntent.FLAG_ONE_SHOT);

    notificationBuilder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_mqtt_battery)
            .setContentTitle(title)
            .setContentText(body)
                .setContentIntent(pendingIntent)  //launch main activity
            .setPriority(PRIORITY_HIGH)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
          CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
      notificationManager.createNotificationChannel(notificationChannel);
    }

    util.LOG("foreService: starting notification...");
    startForeground(NOTIFICATION_ID, notificationBuilder.build());
  }


}
