package cleantool.su.starcleanmaster.util;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmTaskUtil {

    public static void starAlarmTaskByService(Context context, long intervalMinute, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + (intervalMinute * 60 * 1000);

        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    public static void starRepeatAlarmTaskByService(Context context, long intervalMinute, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + (intervalMinute * 60 * 1000);

        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMinute, operation);
        }
    }

    /**
     * RepeatAlarmTask
     *
     * @param context
     * @param beginHour   00-23 Hour
     * @param beginMinute 00-59 Minute
     * @param intent
     */
    public static void starRepeatAlarmTaskByService(Context context, int hour, int minute, long intervalMillis, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();

        PendingIntent operation;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        } else {
            operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, operation);
        }
    }

    public static void stopAlarmTaskByService(Context context, Intent intent) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.cancel(operation);
    }
}
