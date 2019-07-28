package com.salman.weatherforecaster.common;

import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    private static final String TAG = "Common";
    public static final String APP_ID = "15cd71d20be4de764d3724236f7e34e3";
    public Location current_location;

    public static String convertUnixToDate(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy K:mm a");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToHour(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToDay(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        String formatted = sdf.format(date);
        return formatted;
    }

    public Location getCurrent_location() {
        return current_location;
    }

    public void setCurrent_location(Location location) {
        this.current_location = location;

    }

}

//"HH:mm EEE MM yyyy"
