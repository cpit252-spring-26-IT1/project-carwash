package sa.edu.kau.fcit.cpit252.carwash.observer;

import org.junit.Test;

import static org.junit.Assert.*;

public class WashEventBusTest {

    private static class RecordingListener implements WashEventListener {
        int deductedCount;
        int completedCount;
        String lastOrderId;
        String lastCustomerName;
        String lastPackageName;

        @Override
        public void onWashDeducted(String orderId, String customerName, String packageName) {
            deductedCount++;
            lastOrderId = orderId;
            lastCustomerName = customerName;
            lastPackageName = packageName;
        }

        @Override
        public void onOrderCompleted(String orderId) {
            completedCount++;
            lastOrderId = orderId;
        }
    }

    @Test
    public void getInstance_shouldReturnSameSingletonObject() {
        WashEventBus first = WashEventBus.getInstance();
        WashEventBus second = WashEventBus.getInstance();

        assertSame(first, second);
    }

    @Test
    public void publishWashDeducted_shouldNotifySubscribedListenerWithCorrectData() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingListener listener = new RecordingListener();

        bus.unsubscribe(listener);
        bus.subscribe(listener);
        bus.publishWashDeducted("ORDER-1", "Ali", "Full Service");
        bus.unsubscribe(listener);

        assertEquals(1, listener.deductedCount);
        assertEquals("ORDER-1", listener.lastOrderId);
        assertEquals("Ali", listener.lastCustomerName);
        assertEquals("Full Service", listener.lastPackageName);
    }

    @Test
    public void publishOrderCompleted_shouldNotifySubscribedListenerWithCorrectOrderId() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingListener listener = new RecordingListener();

        bus.subscribe(listener);
        bus.publishOrderCompleted("ORDER-2");
        bus.unsubscribe(listener);

        assertEquals(1, listener.completedCount);
        assertEquals("ORDER-2", listener.lastOrderId);
    }

    @Test
    public void subscribe_shouldNotAddSameListenerTwice() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingListener listener = new RecordingListener();

        bus.unsubscribe(listener);
        bus.subscribe(listener);
        bus.subscribe(listener);
        bus.publishWashDeducted("ORDER-3", "Sara", "Exterior Only");
        bus.unsubscribe(listener);

        assertEquals("Same listener should only receive the event once", 1, listener.deductedCount);
    }

    @Test
    public void unsubscribe_shouldStopReceivingEvents() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingListener listener = new RecordingListener();

        bus.subscribe(listener);
        bus.unsubscribe(listener);
        bus.publishOrderCompleted("ORDER-4");

        assertEquals(0, listener.completedCount);
    }

    @Test
    public void subscribe_shouldIgnoreNullListener() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingListener listener = new RecordingListener();

        bus.subscribe(null);
        bus.subscribe(listener);
        bus.publishOrderCompleted("ORDER-5");
        bus.unsubscribe(listener);

        assertEquals(1, listener.completedCount);
    }
}
