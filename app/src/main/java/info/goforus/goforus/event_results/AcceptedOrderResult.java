package info.goforus.goforus.event_results;

import info.goforus.goforus.models.orders.Order;

public class AcceptedOrderResult {

    private final Order mOrder;

    public AcceptedOrderResult(Order order) { mOrder = order; }

    public Order getOrder() { return mOrder; }
}