package info.goforus.goforus.models.conversations;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import org.parceler.Parcel;

import java.lang.reflect.Field;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Parcel(Parcel.Serialization.BEAN)
public class Message extends Model {

    @Column(name = "externalId", index = true, unique=  true)
    public int externalId;
    @Column(name = "isMe", index = true)
    public boolean isMe;
    @Column(name = "isUnread", index = true)
    public boolean isUnread;
    @Column(name = "body")
    public String body;
    @Column(name = "Conversation")
    public Conversation conversation;
    @Column(name = "notificationSent")
    public boolean noticiationSent = false;

    public Message(){
        super();
    }

    public Message(JSONObject message, Conversation conversation){
        super();
        try {
            this.externalId = message.getInt("id");
            this.isMe       = message.getBoolean("is_me");
            this.isUnread   = message.getBoolean("is_unread");
            this.body       = message.getString("body");
            this.conversation = conversation;

        } catch (JSONException e) {
            e.printStackTrace();
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
    public static Message findOrCreateFromJson(JSONObject json, Conversation conversation) {
        int externalId = 0;

        try {
            externalId = json.getInt("id");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }

        Message existingMessage =
                new Select().from(Message.class).where("externalId = ?", externalId).executeSingle();
        if (existingMessage != null) {
            return existingMessage;
        } else {
            Message message = new Message(json, conversation);
            message.save();
            return message;
        }
    }
}
