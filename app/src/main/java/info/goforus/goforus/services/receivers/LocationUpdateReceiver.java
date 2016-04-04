package info.goforus.goforus.services.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.goforus.goforus.services.LocationUpdateService;

public class LocationUpdateReceiver extends BroadcastReceiver {
    public static final String ACTION = "info.goforus.goforus.services.LocationUpdateServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationUpdateService.class);
        context.startService(i);
    }
}

