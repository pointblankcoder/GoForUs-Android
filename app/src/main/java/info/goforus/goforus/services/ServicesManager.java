package info.goforus.goforus.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import info.goforus.goforus.Application;
import info.goforus.goforus.services.receivers.ConversationsUpdateReceiver;
import info.goforus.goforus.services.receivers.DriversUpdateReceiver;
import info.goforus.goforus.services.receivers.LocationUpdateReceiver;
import info.goforus.goforus.services.receivers.NotificationsUpdateReceiver;

public class ServicesManager {

    private static final ServicesManager manager = new ServicesManager();
    private ServicesManager() {}
    public static ServicesManager getInstance() {
        return manager;
    }
    private Application appContext = Application.getInstance();

    private static final int DRIVERS_UPDATE_REQUEST_CODE = 2558;
    private static final int CONVERSATIONS_UPDATE_REQUEST_CODE = 2557;
    private static final int LOCATION_UPDATE_REQUEST_CODE = 2556;
    private static final int NOTIFICATIONS_UPDATE_REQUEST_CODE = 2555;


    public void scheduleRuntimeRequirments(){
        scheduleNotificationsAlarm();
        scheduleLocationUpdateAlarm();
    }

    // runs every 2s
    public void scheduleDriverUpdateAlarm() {
        int mUpdateInterval = ((1000) * 2);
        Intent intent = new Intent(appContext, DriversUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, DRIVERS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, mUpdateInterval, pIntent);
    }

    public void cancelDriverUpdateAlarm() {
        Intent intent = new Intent(appContext, DriversUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, DRIVERS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    // runs every 10s
    public void scheduleLocationUpdateAlarm() {
        int mUpdateInterval = (((1000) * 10));
        Intent intent = new Intent(appContext, LocationUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, LOCATION_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, mUpdateInterval, pIntent);
    }

    public void cancelLocationUpdateAlarm() {
        Intent intent = new Intent(appContext, NotificationsUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, LOCATION_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    // runs every 30s
    public void scheduleConversationsUpdateAlarm() {
        int mUpdateInterval = (((1000) * 30));
        Intent intent = new Intent(appContext, ConversationsUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, CONVERSATIONS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, mUpdateInterval, pIntent);
    }

    public void cancelConversationsUpdateAlarm() {
        Intent intent = new Intent(appContext, ConversationsUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, CONVERSATIONS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    // runs every 15 minutes
    public void scheduleNotificationsAlarm() {
        int mUpdateInterval = (((1000) * 60) * 15);
        Intent intent = new Intent(appContext, NotificationsUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, NOTIFICATIONS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, mUpdateInterval, pIntent);
    }

    public void cancelNotificationsAlarm() {
        Intent intent = new Intent(appContext, NotificationsUpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(appContext, NOTIFICATIONS_UPDATE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

}
