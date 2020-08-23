package com.hjgode.BattmonMqtt;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyJSON {
    String level="";
    String status="";
    String datetime="";
    public MyJSON(){

    }
    public static String getJSON(BatteryInfo.BattInfo battInfo){
        util.LOG( battInfo.toString());
        String jsonString="";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        //String timestamp=LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("level", battInfo.level);
            jsonObject.put("status", (battInfo.charging?"charging":"discharging"));
            jsonObject.put("datetime", timestamp);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        jsonString=jsonObject.toString();
        util.LOG( "JSON is "+jsonString);
        return jsonString;
    }
}
