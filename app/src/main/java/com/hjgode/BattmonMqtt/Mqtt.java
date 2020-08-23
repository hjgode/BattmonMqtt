package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.UUID;

public class Mqtt {
    public static void doPublish(Context context){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        util.LOG("doPublish-Prefs="+util.dumpPrefs(sharedPreferences));
        String host=sharedPreferences.getString(context.getResources().getString(R.string.mqtt_host),
                context.getResources().getString(R.string.mqtt_default_host));
        int port=sharedPreferences.getInt(context.getResources().getString(R.string.mqtt_port),
                Integer.parseInt(context.getResources().getString(R.string.mqtt_default_port)));
        String topic = "android/batteries/"+sharedPreferences.getString(context.getResources().getString(R.string.mqtt_topic),
                context.getResources().getString(R.string.mqtt_default_topic));
        Mqtt3AsyncClient client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost(host)
                .serverPort(port)
                .buildAsync();
        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        // Handle connection failure
                    } else {
                        byte[] myPayload=MyJSON.getJSON(BatteryInfo.getBattInfo(context)).getBytes();
                        // Setup subscribes or start publishing
                        client.publishWith()
                                .topic(topic)
                                .payload(myPayload)
                                .qos(MqttQos.EXACTLY_ONCE)
                                .send()
                                .whenComplete((mqtt3Publish, throwableP) -> {
                                    if (throwableP != null) {
                                        // Handle failure to publish
                                    } else {
                                        // Handle successful publish, e.g. logging or incrementing a metric
                                        client.disconnect();
                                    }
                                });//publish
                    };
                });//coplete client connect
    }

}
