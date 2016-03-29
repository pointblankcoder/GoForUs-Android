package info.goforus.goforus;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.orhanobut.logger.Logger;

import info.goforus.goforus.models.conversations.DataDistributor;
import info.goforus.goforus.settings.Gps;
import info.goforus.goforus.services.ConversationsAlarmReceiver;

public class Application extends com.activeandroid.app.Application {
    static final String TAG = "Application";

    private static Application instance;
    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public boolean isReady() {
        return Gps.turnedOn();
    }

    public static Application getInstance(){
        return instance;
    }

    @Override
    @SuppressWarnings("ResourceType")
    public void onCreate() {
        super.onCreate();
        Logger.init();

        ActiveAndroid.initialize(this);

        instance = this;

        scheduleConversationsAlarm(0);
        DataDistributor.getInstance().startDistribution();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    // Setup a recurring updates every 15 minutes
    public void scheduleConversationsAlarm(int intervalUpdate) {
        int mUpdateInterval = intervalUpdate;
        if(intervalUpdate == 0){
            mUpdateInterval = (((1000) * 60) * 15);
        }
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), ConversationsAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, ConversationsAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, mUpdateInterval, pIntent);
    }

    public void cancelConversationsAlarm() {
        Intent intent = new Intent(getApplicationContext(), ConversationsAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, ConversationsAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
