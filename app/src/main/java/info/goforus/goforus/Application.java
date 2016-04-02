package info.goforus.goforus;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import com.activeandroid.ActiveAndroid;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.tasks.LocationUpdateHandler;
import info.goforus.goforus.services.ServicesManager;
import info.goforus.goforus.settings.Gps;
import info.goforus.goforus.tasks.NetworkUpdateHandler;

public class Application extends com.activeandroid.app.Application {
    static final String TAG = "Application";

    public LocationUpdateHandler LocationUpdateHandler;
    public ServicesManager ServicesManager;
    public ConnectivityManager ConnectivityManager;
    private static Application instance;
    private JobManager jobManager;

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


    public static boolean isConnected = true;
    @Override
    @SuppressWarnings("ResourceType")
    public void onCreate() {
        super.onCreate();
        Logger.init();

        ActiveAndroid.initialize(this);
        instance = this;

        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();

        ServicesManager = info.goforus.goforus.services.ServicesManager.getInstance();
        ServicesManager.scheduleAll();

        LocationUpdateHandler = info.goforus.goforus.tasks.LocationUpdateHandler.getInstance();
        ConnectivityManager   = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkUpdateHandler.getInstance().startUpdates();

        configureJobManager();
    }

    private void configureJobManager() {
        Configuration.Builder builder = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Logger.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Logger.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Logger.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120);//wait 2 minute
        jobManager = new JobManager(builder.build());
    }

    public JobManager getJobManager() {
        return jobManager;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
