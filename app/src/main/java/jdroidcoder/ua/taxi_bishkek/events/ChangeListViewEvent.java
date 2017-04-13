package jdroidcoder.ua.taxi_bishkek.events;

/**
 * Created by jdroidcoder on 11.04.17.
 */

public class ChangeListViewEvent {
    private boolean isOrders;

    public ChangeListViewEvent(boolean isOrders) {
        this.isOrders = isOrders;
    }

    public boolean isOrders() {
        return isOrders;
    }

    public void setOrders(boolean orders) {
        isOrders = orders;
    }
}
