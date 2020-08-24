package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Mqtt {
    final static String WAKELOCK_TAG="MQTT:publish";
    static LocalBroadcastManager broadcaster;

    public static void sendResult(String message) {
        Intent intent = new Intent(Constants.RESULT_INTENT);
        if(message != null)
            intent.putExtra(Constants.SERVICE_MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    public static void doPublish(Context context){
        broadcaster = LocalBroadcastManager.getInstance(context);
        util.LOG("entering doPublish");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        sendResult(timestamp+" entering doPublish");
        //use a wakelock to keep the device CPU running for the calls...
        PowerManager.WakeLock wakeLock=null;
        try {
            PowerManager pm = (PowerManager)context.getSystemService(
                    Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK
                            | PowerManager.ON_AFTER_RELEASE,
                    WAKELOCK_TAG);
            wakeLock.acquire(10*1000);
            util.LOG("doPublish: wakeLock acquire done");
            sendResult("doPublish: wakeLock acquire done");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            util.LOG("doPublish-Prefs=" + util.dumpPrefs(sharedPreferences));
            sendResult("doPublish-Prefs=" + util.dumpPrefs(sharedPreferences));
            String host = sharedPreferences.getString(context.getResources().getString(R.string.mqtt_host),
                    context.getResources().getString(R.string.mqtt_default_host));

            String portS = sharedPreferences.getString(context.getResources().getString(R.string.mqtt_port),
                    context.getResources().getString(R.string.mqtt_default_port));
            int port=1883;
            try{
                port=Integer.parseInt(portS);
            }catch (Exception ex){

            }
            String topic = "android/batteries/" + sharedPreferences.getString(context.getResources().getString(R.string.mqtt_topic), context.getResources().getString(R.string.mqtt_default_topic));
            util.LOG("doPublish: topic="+topic+", port="+port);
            sendResult("doPublish: topic="+topic+", port="+port);
            Mqtt3AsyncClient client = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost(host)
                    .serverPort(port)
                    .buildAsync();
            PowerManager.WakeLock finalWakeLock = wakeLock;
            util.LOG("doPublish: connect()...");
            sendResult("doPublish: connect()...");
            client.connect()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            // Handle connection failure
                            util.LOG("doPublish: connect()...failed: "+throwable.getMessage());
                            sendResult("doPublish: connect()...failed: "+throwable.getMessage());
                            finalWakeLock.release();
                        } else {
                            util.LOG("doPublish: connect()...success...");
                            sendResult("doPublish: connect()...success...");
                            byte[] myPayload = MyJSON.getJSON(BatteryInfo.getBattInfo(context)).getBytes();
                            // Setup subscribes or start publishing
                            util.LOG("doPublish: publish()...");
                            client.publishWith()
                                    .topic(topic)
                                    .payload(myPayload)
                                    .qos(MqttQos.EXACTLY_ONCE)
                                    .send()
                                    .whenComplete((mqtt3Publish, throwableP) -> {
                                        if (throwableP != null) {
                                            // Handle failure to publish
                                            util.LOG("doPublish: publish()...failed: "+throwableP.getMessage());
                                            sendResult("doPublish: publish()...failed: "+throwableP.getMessage());
                                            finalWakeLock.release();
                                        } else {
                                            // Handle successful publish, e.g. logging or incrementing a metric
                                            //client.disconnect();
                                            util.LOG("doPublish: disconnect()...");
                                            sendResult("doPublish: disconnect()...");
                                            client.disconnect().whenComplete((disconnectComplete, throwableD)->{
                                                if(throwableD!=null) {
                                                    finalWakeLock.release();
                                                    util.LOG("doPublish: disconnect()...failed: "+throwableD.getMessage());
                                                    sendResult("doPublish: disconnect()...failed: "+throwableD.getMessage());
                                                }else{
                                                    util.LOG("doPublish: disconnect()...success. WakeLock.release()");
                                                    sendResult("doPublish: disconnect()...success. WakeLock.release()");
                                                    finalWakeLock.release();
                                                }
                                            });//disconnect complete
                                        }
                                    });//publish complete
                        }//pub
                    });//complete client connect
            finalWakeLock.release();
        }catch(Exception ex){
            wakeLock.release();
        }
        util.LOG("doPublish: ...END");
    }

}
