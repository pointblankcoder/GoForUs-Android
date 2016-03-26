package info.goforus.goforus.tasks;

import android.os.Handler;
import android.util.Log;

import info.goforus.goforus.Application;
import info.goforus.goforus.models.account.Account;

public class DriverUpdatesTask implements Runnable {
    private static final String TAG = "DriverUpdatesTask";

    private static Handler mHandler;
    private static int mInterval;

    public DriverUpdatesTask(Handler handler, int intervalTimeInMilliseconds) {
        mHandler = handler;
        mInterval = intervalTimeInMilliseconds;
    }

    @Override
    public void run(){
        // Gets data from the incoming Intent
        Log.d(TAG, "Running Driver Updates Task");
        Account account = Account.currentAccount();
        Log.d(TAG, String.format("account(%s)", account));
        if (account != null) {
            Log.d(TAG, "getting nearby drivers");
            try {
                Application.mApi.getNearbyDrivers(account.lat, account.lng);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        mHandler.postDelayed(this, mInterval);
    }
}
