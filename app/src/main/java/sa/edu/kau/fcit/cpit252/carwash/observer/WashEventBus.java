package sa.edu.kau.fcit.cpit252.carwash.observer;

import java.util.ArrayList;
import java.util.List;

public class WashEventBus {
    private static WashEventBus instance;

    private final List<WashEventListener> listeners = new ArrayList<>();

    private WashEventBus() {}

    public static synchronized WashEventBus getInstance() {
        if (instance == null) {
            instance = new WashEventBus();
        }
        return instance;
    }

    public void subscribe(WashEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(WashEventListener listener) {
        listeners.remove(listener);
    }

    public void publishWashDeducted(String orderId, String customerName, String packageName) {
        for (WashEventListener l : new ArrayList<>(listeners)) {
            l.onWashDeducted(orderId, customerName, packageName);
        }
    }

    public void publishOrderCompleted(String orderId) {
        for (WashEventListener l : new ArrayList<>(listeners)) {
            l.onOrderCompleted(orderId);
        }
    }
}
