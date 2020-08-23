package com.hjgode.BattmonMqtt;

import android.content.Context;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.UUID;

public class Mqtt {
    public static void doPublish(Context context){
        Mqtt3AsyncClient client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost("192.168.0.40")
                .serverPort(1883)
                .buildAsync();
        client.connect()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        // Handle connection failure
                    } else {
                        byte[] myPayload=MyJSON.getJSON(BatteryInfo.getBattInfo(context)).getBytes();
                        // Setup subscribes or start publishing
                        client.publishWith()
                                .topic("android/batteries/ocean")
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
