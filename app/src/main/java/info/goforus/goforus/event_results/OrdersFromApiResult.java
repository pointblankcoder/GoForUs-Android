package info.goforus.goforus.event_results;

import java.util.ArrayList;
import java.util.List;

import info.goforus.goforus.models.jobs.Job;
import info.goforus.goforus.models.orders.Order;
import us.monoid.json.JSONArray;

public class OrdersFromApiResult {

    List<Order> mOrders = new ArrayList<>();

    public OrdersFromApiResult(JSONArray jobsJSON) {
        if (jobsJSON != null) {
            mOrders = Order.updateOrCreateAllFromJson(jobsJSON);
        }
    }

    public List<Order> getOrders() { return mOrders; }
}