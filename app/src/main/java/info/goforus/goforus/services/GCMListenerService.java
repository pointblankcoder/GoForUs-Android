/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.goforus.goforus.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.BaseActivity;
import info.goforus.goforus.R;
import info.goforus.goforus.event_results.AcceptedOrderResult;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.DeclinedOrderResult;
import info.goforus.goforus.event_results.JobsFromApiResult;
import info.goforus.goforus.event_results.MessagesFromApiResult;
import info.goforus.goforus.event_results.OrdersFromApiResult;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class GCMListenerService extends GcmListenerService {

    private static final String TAG = "GCMListenerService";
    private static final String NEW_CONVERSATION = "New Conversation";
    private static final String NEW_MESSAGE = "New Message";
    private static final String NEW_JOB = "New Job";
    private static final String UPDATED_JOB = "Updated Job";
    private static final String NEW_ORDER = "New Order";
    private static final String UPDATED_ORDER = "Updated Order";
    private static final String ACCEPTED_ORDER = "Accepted Order";
    private static final String DECLINED_ORDER = "Declined Order";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String messageType = data.getString("type");

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            if (messageType != null) {
                switch (messageType) {
                    case NEW_CONVERSATION:
                        try {
                            Logger.i("Received a new conversation");
                            JSONObject conversationJSON = new JSONObject(data.getString("conversation"));
                            JSONArray conversationJSONArray = new JSONArray();
                            conversationJSONArray.put(conversationJSON);

                            EventBus.getDefault().post(new ConversationsFromApiResult(conversationJSONArray));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case NEW_MESSAGE:
                        try {
                            Logger.i("Received a new message");
                            JSONObject messageJSON = new JSONObject(data.getString("message"));
                            int conversationId = messageJSON.getInt("conversation_id");
                            String messageBody = messageJSON.getString("body");
                            JSONArray messageJSONArray = new JSONArray();
                            messageJSONArray.put(messageJSON);

                            EventBus.getDefault().post(new MessagesFromApiResult(messageJSONArray, conversationId));
                            sendNotification(NEW_MESSAGE, messageBody);
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case NEW_JOB:
                        try {
                            Logger.i("Received a new job");
                            JSONObject jobJSON = new JSONObject(data.getString("job"));
                            JSONArray jobJSONArray = new JSONArray();
                            jobJSONArray.put(jobJSON);

                            EventBus.getDefault().post(new JobsFromApiResult(jobJSONArray));
                            sendNotification(NEW_JOB, "New Job");
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case UPDATED_JOB:
                        try {
                            Logger.i("Received a updated job");
                            JSONObject jobJSON = new JSONObject(data.getString("job"));
                            JSONArray jobJSONArray = new JSONArray();
                            jobJSONArray.put(jobJSON);
                            EventBus.getDefault().post(new JobsFromApiResult(jobJSONArray));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case NEW_ORDER:
                        try {
                            Logger.i("Received a new order");
                            JSONObject orderJSON = new JSONObject(data.getString("order"));
                            JSONArray orderJSONArray = new JSONArray();
                            orderJSONArray.put(orderJSON);

                            EventBus.getDefault().post(new OrdersFromApiResult(orderJSONArray));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case UPDATED_ORDER:
                        try {
                            Logger.i("Received a update order");
                            JSONObject orderJSON = new JSONObject(data.getString("order"));
                            JSONArray orderJSONArray = new JSONArray();
                            orderJSONArray.put(orderJSON);

                            EventBus.getDefault().post(new OrdersFromApiResult(orderJSONArray));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }

                        break;
                    case ACCEPTED_ORDER:
                        try {
                            Logger.i("Received an accepted order");
                            JSONObject jobJSON = new JSONObject(data.getString("order"));

                            Order order = Order.updateOrder(jobJSON);
                            EventBus.getDefault().post(new AcceptedOrderResult(order));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                    case DECLINED_ORDER:
                        try {
                            Logger.i("Received an declined order");
                            JSONObject jobJSON = new JSONObject(data.getString("order"));

                            Order order = Order.updateOrder(jobJSON);
                            EventBus.getDefault().post(new DeclinedOrderResult(order));
                        } catch (JSONException e) {
                            Logger.e(e.toString());
                        }
                        break;
                }
            }
        }
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(message)
                .setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(9393939 /* ID of notification */, notificationBuilder.build());
    }
}
