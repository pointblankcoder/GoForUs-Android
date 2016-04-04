package info.goforus.goforus.tasks;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.util.AsyncExecutor;

import info.goforus.goforus.Application;
import info.goforus.goforus.event_results.NetworkUpdateResult;

public class NetworkUpdateHandler {

    private static final long REPEAT_TIME = 1000;
    private static NetworkUpdateHandler ourInstance = new NetworkUpdateHandler();
    public static NetworkUpdateHandler getInstance() {
        return ourInstance;
    }
    private NetworkUpdateHandler() {
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
                    NetworkInfo activeNetwork = Application.getInstance().ConnectivityManager.getActiveNetworkInfo();
                    Application.isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());

                    EventBus.getDefault().post(new NetworkUpdateResult(activeNetwork, Application.isConnected));

                    mHandler.removeCallbacks(task);
                    mHandler.postDelayed(task, REPEAT_TIME);
                }
            });
        }
    };
}
