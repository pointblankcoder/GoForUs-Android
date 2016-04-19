package info.goforus.goforus.tasks;

import android.os.Handler;

import org.greenrobot.eventbus.util.AsyncExecutor;

import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.models.accounts.Account;

public class DriversUpdateHandler {
    private static final long REPEAT_TIME = 1000;
    private static DriversUpdateHandler ourInstance = new DriversUpdateHandler();
    public static DriversUpdateHandler getInstance() {
        return ourInstance;
    }
    private DriversUpdateHandler() {
    }

    Handler mHandler = new Handler();

    public void startUpdates(){
        mHandler.postDelayed(task, REPEAT_TIME);
    }

    public void stopUpdates(){
        mHandler.removeCallbacks(task);
    }

    final Runnable task = new Runnable() {
        @Override
        public void run() {
            AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                @Override
                public void run() throws Exception {
                    Account account = Account.currentAccount();
                    if (account != null) {
                        Utils.LOCATIONS_API.getNearbyDrivers(account.lat, account.lng);
                    }
                    mHandler.postDelayed(task, REPEAT_TIME);
                }
            });
        }
    };
}
