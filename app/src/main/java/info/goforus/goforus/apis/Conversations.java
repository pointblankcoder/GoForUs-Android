package info.goforus.goforus.apis;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import info.goforus.goforus.GoForUs;
import info.goforus.goforus.R;
import info.goforus.goforus.event_results.MessageSentResult;
import us.monoid.json.JSONArray;
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

        return response;
    }

    public void sendMessage(final String messageToSend, int conversationId) throws Throwable {
        JSONObject response;
        JSONObject baseJson = new JSONObject();

        baseJson.put("conversation_id", conversationId);
        baseJson.put("message", messageToSend);

        response = Utils.resty.json(replyURI + Utils.tokenParams(), put(content(baseJson))).toObject();

        if (response.getString("status").equals("ok")) {
            EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_OK, GoForUs.getInstance().getString(R.string.message_sent)));
        } else {
            Logger.e("something went wrong");
            Logger.e(String.valueOf(response));
            EventBus.getDefault().post(new MessageSentResult(MessageSentResult.RESULT_FAILURE, GoForUs
                    .getInstance().getString(R.string.standard_failure_response)));
        }
    }
}
