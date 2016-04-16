package info.goforus.goforus.event_results;

import info.goforus.goforus.models.orders.Order;

public class DeclinedOrderResult {

    private final Order mOrder;

    public DeclinedOrderResult(Order order) { mOrder = order; }

    public Order getOrder() { return mOrder; }
}