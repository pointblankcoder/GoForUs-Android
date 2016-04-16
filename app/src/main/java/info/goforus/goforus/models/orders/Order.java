package info.goforus.goforus.models.orders;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.orhanobut.logger.Logger;

import info.goforus.goforus.models.accounts.Account;
import info.goforus.goforus.models.conversations.Conversation;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

@Table(name = "Orders")
public class Order extends Model {

    @Column(name = "externalId", index = true) public int externalId;
    @Column(name = "partnerId", index = true) public int partnerId; // External
    @Column(name = "customerId", index = true) public int customerId; // External
    @Column(name = "conversationId") public long conversationId; // External
    @Column(name = "estimatedCost") public float estimatedCost;
    @Column(name = "finalCost") public float finalCost;
    @Column(name = "accepted") public boolean accepted = false;
    @Column(name = "inProgress") public boolean inProgress = false;
    @Column(name = "description") public String description;

    @Column(name = "pickupLocationLat") public double pickupLocationLat;
    @Column(name = "pickupLocationLng") public double pickupLocationLng;
    @Column(name = "pickupAddress") public String pickupAddress;

    @Column(name = "dropOffLocationLat") public double dropOffLocationLat;
    @Column(name = "dropOffLocationLng") public double dropOffLocationLng;
    @Column(name = "dropOffAddress") public String dropOffAddress;

    public Order() {
        super();
    }

    public Order(JSONObject jsonObject) {
        super();

        try {
            externalId = jsonObject.getInt("id");
            partnerId = jsonObject.getInt("partner_id");
            customerId = jsonObject.getInt("customer_id");
            conversationId = jsonObject.getInt("conversation_id");
            estimatedCost = (float) jsonObject.getDouble("estimated_cost");
            finalCost = (float) jsonObject.getDouble("final_cost");
            accepted = jsonObject.getBoolean("accepted");
            inProgress = jsonObject.getBoolean("in_progress");
            if (jsonObject.has("description")) description = jsonObject.getString("description");

            pickupLocationLat = jsonObject.getDouble("pickup_location_lat");
            pickupLocationLng = jsonObject.getDouble("pickup_location_lng");
            pickupAddress = jsonObject.getString("pickup_address");

            dropOffLocationLat = jsonObject.getDouble("dropoff_location_lat");
            dropOffLocationLng = jsonObject.getDouble("dropoff_location_lng");
            dropOffAddress = jsonObject.getString("dropoff_address");
        } catch (JSONException e) {
            Logger.e(e.toString());
        }
    }


    public static Order updateOrder(JSONObject jsonObject) {
        Order tmpOrder = new Order(jsonObject);
        Order order = Order.findByExternalId(tmpOrder.externalId);
        order.partnerId = tmpOrder.partnerId;
        order.customerId = tmpOrder.customerId;
        order.conversationId = tmpOrder.conversationId;
        order.estimatedCost = tmpOrder.estimatedCost;
        order.finalCost = tmpOrder.finalCost;
        order.accepted = tmpOrder.accepted;
        order.inProgress = tmpOrder.inProgress;
        order.description = tmpOrder.description;
        order.pickupLocationLat = tmpOrder.pickupLocationLat;
        order.pickupLocationLng = tmpOrder.pickupLocationLng;
        order.pickupAddress = tmpOrder.pickupAddress;
        order.dropOffLocationLat = tmpOrder.dropOffLocationLat;
        order.dropOffLocationLng = tmpOrder.dropOffLocationLng;
        order.dropOffAddress = tmpOrder.dropOffAddress;
        order.save();
        return order;
    }

    public static Order lastAwaitingConfirmed(int partnerId) {
        return new Select().from(Order.class)
                           .where("partnerId = ? AND accepted = ? AND customerId = ?", partnerId, false, Account
                                   .currentAccount().externalId).executeSingle();
    }

    public static Order findByConversation(Conversation conversation) {
        return new Select().from(Order.class).where("conversationId = ?", conversation.externalId)
                           .executeSingle();
    }

    public static Order findByExternalId(int externalId) {
        return new Select().from(Order.class).where("externalId = ?", externalId).executeSingle();
    }
}
