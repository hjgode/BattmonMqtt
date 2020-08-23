package com.hjgode.BattmonMqtt;

import android.content.Context;
import android.os.BatteryManager;

import static android.content.Context.BATTERY_SERVICE;

public class BatteryInfo {

    public static BattInfo getBattInfo(Context context){
        int l=0;boolean status=false;
        BatteryManager bm = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            status = bm.isCharging();
            l=percentage;
        }
        util.LOG( "getLevel="+l+ (status?" charging":" discharging"));
        return new BattInfo(l, status);
    }

    public static class BattInfo{
        public int level=0;
        public boolean charging=false;
        public BattInfo(int l, boolean c){
            level=l;
            charging=c;
        }
        @Override
        public String toString(){
            return level +"% "+ (charging?"charging":"discharging");
        }
    }
}
