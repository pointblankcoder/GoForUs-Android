package info.goforus.goforus;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import com.activeandroid.ActiveAndroid;
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
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
