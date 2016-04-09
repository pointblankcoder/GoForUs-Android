package info.goforus.goforus.apis;

import java.io.IOException;

import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.put;

public class Orders {
    private static Orders ourInstance = new Orders();
    public static Orders getInstance() { return ourInstance; }
    private Orders() {}



    public static String postOrderUri() { return Utils.getBaseUri() + "orders"; }
    public static String getOrdersUri() { return Utils.getBaseUri() + "orders"; }

    public JSONObject postOrder(Order order) throws JSONException, IOException {
        JSONObject response;
        JSONObject baseJson = new JSONObject();
        JSONObject orderJson = new JSONObject();

        orderJson.put("partner_id", order.partnerId);
        orderJson.put("customer_id", order.customerId);
        orderJson.put("pickup_location_lat", order.pickupLocationLat);
        orderJson.put("pickup_location_lng", order.pickupLocationLng);
        orderJson.put("dropoff_location_lat", order.dropOffLocationLat);
        orderJson.put("dropoff_location_lng", order.dropOffLocationLng);
        orderJson.put("estimated_cost", order.estimatedCost);
        orderJson.put("message", order.description);
        baseJson.put("order", orderJson);

        response = Utils.resty.json(postOrderUri() + Utils.tokenParams(), put(content(baseJson))).object();

        return response;
    }

}
