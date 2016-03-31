package info.goforus.goforus.services.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.goforus.goforus.services.NotificationsUpdateService;

public class NotificationsUpdateReceiver extends BroadcastReceiver {
    public static final String ACTION = "info.goforus.goforus.services.ConversationsService";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NotificationsUpdateService.class);
        context.startService(i);
    }
}

