#BattmonMqtt
- publish Mqtt periodically with battery level

The app will also publish during the maintanence window in doze mode, but with larger intervals (1-5 hours).

# Battery Monitor for MQTT
BatteryMonMQTT is a small tool that publishes battery data peridically to a MQTT broker, for example mosquitto.

![Main Screen](https://github.com/hjgode/BattmonMqtt/raw/master/doc/main_screen.png)

The periodic action is implemented as a JobService which is scheduled by a JobScheduler. The JobService will not be started, if the device is in Doze mode, but then runs later on inside the Maintenance window of the device. So the app does not cosume much battery.

The JobService is also registered after booting the device. If the app is setup once, you do not need to care about the function.

The only settings you need to do are the topic and the server name or ip.

![Settings Screen](https://github.com/hjgode/BattmonMqtt/raw/master/doc/settings_screen.png)

The tool does not provide secured MQTT although the heavy Paho MQTT library supports that. Feel free to clone the repo and implement user/password.

I started the app thinking it can not be that hard to create a simple periodic update to MQTT. But the devil lives in the details. Paho MQTT is very heavy and needs many additional permissions added to the app. It leaks a service connection after disconnect.

The other hard coding is due to Google's paranoia and the restrictions you will face writing a periodic background service. Especially starting with Android 8.

You may download the 1.1 release with this QR code:
![Download BattmonMQTT v1.1](https://github.com/hjgode/BattmonMqtt/raw/master/doc/barcode.png)

![BattmonMqtt 1.1](https://github.com/hjgode/BattmonMqtt/raw/master/app/build/outputs/apk/debug/BattmonMqtt_20201004.apk)

### Test doze mode:

*Follow the following steps to make the device cycle through LIGHT and DEEP-DOZE modes*

- Connect the device to your machine having ADB
 
- Run your app and leave it active
 
- Turn off the display with the app running

#### Make the device cycle through the doze modes

There are multiple ADB commands that will help us test the application:

Unplugging the battery 

- while being connected to the machine:

Whether you are using an emulator or a physical device, you will need to unplug the battery to allow doze mode to kick in.

    adb shell dumpsys battery unplug

This command will do the needful. Observe that the battery is no longer charging.

But be careful to reset this once you are done testing. Use the following command to reset.

    adb shell dumpsys battery reset

The next step is to make the device cycle through light and deep doze modes

    adb shell dumpsys deviceidle step [light|deep]

This command will step the device through each of the checks before going into idle state. It will also print out the current state after each step.

You can query the current state by using this command:

    adb shell dumpsys deviceidle get [light|deep|force|screen|charging|network]

Each of the doze modes will have multiple states.

    Light: ACTIVE -> IDLE -> IDLE_MAINTENANCE -> OVERRIDE
    Deep: ACTIVE -> IDLE_PENDING -> SENSING -> LOCATING -> IDLE -> IDLE_MAINTENANCE

Once the DEEP-DOZE mode has reached the IDLE state, LIGHT-DOZE mode will have no effect and hence the state is represented as OVERRIDE.

(source: https://medium.com/@mohitgupta92/testing-your-app-on-doze-mode-4ee30ad6a3b0)

## Original base for PeriodicWorker/KeepAlive
https://github.com/dreamuniverse/workmanager

This is a workmanager demo for stay app alive or wake up the app periodically. In some ROMs, it may not be useful because the task manager of them is like the force stop operation in phone settings.
The foreground service is useful for all versions and ROMs.