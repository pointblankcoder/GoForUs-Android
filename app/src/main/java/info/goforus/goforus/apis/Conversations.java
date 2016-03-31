package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.Application;
import info.goforus.goforus.R;
import info.goforus.goforus.models.conversations.Message;
import info.goforus.goforus.event_results.ConversationsFromApiResult;
import info.goforus.goforus.event_results.MessageSentResult;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Conversations {
    public static final String conversationsURI = Utils.BaseURI + "conversations";
    public static final String inboxURI = Utils.BaseURI + "conversations/inbox";
    public static final String replyURI = Utils.BaseURI + "conversations/reply";

    private static final Conversations conversation = new Conversations();

    private Conversations() {
    }

    public static Conversations getInstance() {
        return conversation;
    }

    public JSONArray getInbox() {
        JSONArray response;

        try {
            response = Utils.resty.json(inboxURI + Utils.tokenParams()).array();
        } catch (Exception e) {
            response = new JSONArray();
            Logger.e(e.toString());
        }

        EventBus.getDefault().post(new ConversationsFromApiResult(response));
        return response;
    }


    public JSONArray getInboxSinceId(final int sinceId) {
        JSONArray response;

        try {
            response = Utils.resty.json(inboxURI + Utils.tokenParams() + "&since_id=" + sinceId).array();
        } catch (Exception e) {
            response = new JSONArray();
            Logger.e(e.toString());
        }

        EventBus.getDefault().post(new ConversationsFromApiResult(response));
        return response;
    }

    public void sendMessage(final Message messageToSend) {
        JSONObject response;
        JSONObject baseJson = new JSONObject();
        try {
            baseJson.put("conversation_id", messageToSend.conversation.externalId);
            baseJson.put("message", messageToSend.body);
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        try {
            response = Utils.resty.json(replyURI + Utils.tokenParams(), put(content(baseJson))).toObject();
            if (response.getString("status").equals("ok")) {
                EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_OK, Application.getInstance().getString(R.string.message_sent)));
            } else {
                Logger.e("something went wrong");
                Logger.e(String.valueOf(response));
                EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_FAILURE, Application.getInstance().getString(R.string.standard_failure_response)));
            }
        } catch (Exception e) {
            Logger.e(e.toString());
            EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_FAILURE, Application.getInstance().getString(R.string.standard_failure_response)));
        }
    }
}
