package sa.edu.kau.fcit.cpit252.carwash.observer;

public interface WashEventListener {
    public void onWashDeducted(String orderId, String customerName, String packageName);

    public void onOrderCompleted(String orderId);
}
