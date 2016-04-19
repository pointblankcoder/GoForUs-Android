package info.goforus.goforus.models.conversations;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import org.parceler.Parcel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.accounts.Account;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Parcel(Parcel.Serialization.BEAN)
@Table(name = "Messages")
public class Message extends Model {

    @Column(name = "externalId", index = true, unique = true) public int externalId;
    @Column(name = "isMe", index = true) public boolean isMe;
    @Column(name = "isRead", index = true) public boolean isRead;
    @Column(name = "body") public String body;
    @Column(name = "Conversation") public Conversation conversation;
    @Column(name = "notificationSent") public boolean notificationSent = false;
    @Column(name = "confirmedReceived", index = true) public boolean confirmedReceived = false;

    public boolean shouldAnimateIn = false;

    public Message() {
        super();
    }

    public Message(JSONObject message, Conversation conversation) {
        super();
        try {
            this.externalId = message.getInt("id");
            this.isMe = message.getInt("sender_id") == Account.currentAccount().externalId;
            this.isRead = message.getBoolean("is_read");
            this.body = message.getString("body");
            this.conversation = conversation;
            this.confirmedReceived = true;
        } catch (JSONException e) {
            Logger.e(e.toString());
        }
    }


    @SuppressWarnings("unused")
    private void setId(Parcel in) {
        try {
            Field idField = Model.class.getDeclaredField("mId");
            idField.setAccessible(true);
            idField.set(this, in.value());
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed to get the Active Android ID", e);
        }
    }

    // Finds existing Message based on remoteId or creates new user and returns
    public static Message updateOrCreateFromJson(JSONObject json, int conversationId) {
        Conversation conversation = Conversation.findByExternalId(conversationId);

        String body = "";
        int externalId = 0;

        try {
            body = json.getString("body");
            externalId = json.getInt("id");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        Message existingMessage = new Select().from(Message.class)
                                              .where("body = ? AND confirmedReceived = ?", body, false)
                                              .executeSingle();
        if (existingMessage != null) {

            existingMessage.body = body;
            existingMessage.externalId = externalId;
            existingMessage.confirmedReceived = true;
            existingMessage.save();

            return existingMessage;
        } else {
            Message message = new Message(json, conversation);
            message.save();
            return message;
        }
    }

    public static List<Message> updateOrCreateAllFromJson(JSONArray response, int conversationId) {
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject messageJSON = response.getJSONObject(i);
                Message message = updateOrCreateFromJson(messageJSON, conversationId);
                messages.add(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }
}
