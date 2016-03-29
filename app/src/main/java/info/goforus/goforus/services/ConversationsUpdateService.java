package info.goforus.goforus.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.orhanobut.logger.Logger;

import java.util.List;

import info.goforus.goforus.MessagesFragment;
import info.goforus.goforus.R;
import info.goforus.goforus.apis.Utils;
import info.goforus.goforus.apis.listeners.InboxResponseListener;
import info.goforus.goforus.models.conversations.Conversation;
import info.goforus.goforus.models.conversations.Message;
import us.monoid.json.JSONArray;

public class ConversationsUpdateService extends IntentService implements InboxResponseListener {
    public ConversationsUpdateService() {
        super("ConversationsUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do the task here
        Logger.i("ConversationsUpdateService", "Service running");
        Utils.ConversationsApi.getInbox(this);
    }

    @Override
    public void onInboxResponse(JSONArray response) {
        Logger.i("ConversationsUpdateService");
        Logger.json(String.valueOf(response));

        List<Conversation> conversations = Conversation.findOrCreateAllFromJson(response);
        for (Conversation c : conversations) {
            for (Message m : c.messages()) {
                // TODO: Change how we should send notifications and when (not while in app etc)
                if (m.isUnread && !m.noticiationSent) {
                    createNotification(
                            m.externalId,
                            R.drawable.message_alert,
                            String.format("Message from %s", "NAME HERE"),
                            abbreviateString(m.body, 100)
                    );

                    m.noticiationSent = true;
                    m.save();
                }
            }
        }
    }

    public String abbreviateString(String input, int maxLength) {
        if (input.length() <= maxLength)
            return input;
        else
            return input.substring(0, maxLength - 2) + "..";
    }

    private void createNotification(int nId, int iconRes, String title, String body) {
        Intent intent = new Intent(this, MessagesFragment.class);

        int requestID = (int) System.currentTimeMillis(); //unique requestID to differentiate between various notification with same NotifId
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // cancel old intent and create new one
        PendingIntent pIntent = PendingIntent.getActivity(this, requestID, intent, flags);


        Notification notification = new NotificationCompat.Builder(
                this).setSmallIcon(iconRes)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(body).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        Logger.i("ConversationsUpdateService", "Service stopping");
        super.onDestroy();
    }
}
