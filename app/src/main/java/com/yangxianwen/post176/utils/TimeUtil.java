package com.yangxianwen.post176.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {


    public static boolean inTime(String start, String end, String pattern) {
        if (start == null || end == null) {
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        String current = format.format(Calendar.getInstance().getTime());
        Date startData;
        Date endData;
        Date currentData;

        try {
            startData = format.parse(start);
            endData = format.parse(end);
            currentData = format.parse(current);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (startData == null || endData == null || currentData == null) {
            return false;
        }

        Calendar cStart = Calendar.getInstance();
        cStart.setTime(startData);
        Calendar cEnd = Calendar.getInstance();
        cEnd.setTime(endData);
        Calendar cCurrent = Calendar.getInstance();
        cCurrent.setTime(currentData);

        return cCurrent.getTimeInMillis() >= cStart.getTimeInMillis() && cCurrent.getTimeInMillis() <= cEnd.getTimeInMillis();
    }

    public static String getDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String[] getStringTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        return new String[]{String.valueOf(calendar.getTimeInMillis()), simpleDateFormat.format(calendar.getTime())};
    }
}
