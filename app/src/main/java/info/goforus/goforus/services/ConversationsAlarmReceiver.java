package info.goforus.goforus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConversationsAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 2555;
    public static final String ACTION = "info.goforus.goforus.services.ConversationsService";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ConversationsUpdateService.class);
        context.startService(i);
    }
}

