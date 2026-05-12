package sa.edu.kau.fcit.cpit252.carwash.database;

import android.media.MediaPlayer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class OrderManager {

    private final FirebaseFirestore db;

    public OrderManager() {
        this.db = DatabaseManager.getInstance().getDb();
    }

    public interface OperationCallback {
        void onSuccess();
        void onFailure(String reason);
    }
    public static void saveNewOrder(String userId, String packageName, String packagePrice, String vehicle, OnCompleteListener<Void> completionListener) {

        FirebaseFirestore db = DatabaseManager.getInstance().getDb();

        DocumentReference newOrderRef = db.collection("Orders").document();
        String orderId = newOrderRef.getId();
        String shortCode = orderId.substring(0, 6).toUpperCase();

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("userId", userId);
        orderDetails.put("packageName", packageName);
        orderDetails.put("packagePrice", packagePrice);
        orderDetails.put("vehicle", vehicle);
        orderDetails.put("washesUsed", "0");
        orderDetails.put("maxWashes", "5");
        orderDetails.put("shortCode", shortCode);
        orderDetails.put("status", "active");
        orderDetails.put("createdAt", FieldValue.serverTimestamp());

        newOrderRef.set(orderDetails).addOnCompleteListener(completionListener);
    }

    public void deductWash(String orderId, OperationCallback callback) {
        DocumentReference ref = db.collection("Orders").document(orderId);
        ref.get().addOnSuccessListener(snap -> {
            if (!snap.exists()) {
                callback.onFailure("Order not found");
                return;
            }
            String usedStr = snap.getString("washesUsed");
            String maxStr = snap.getString("maxWashes");
            int used = usedStr == null ? 0 : Integer.parseInt(usedStr);
            int max = maxStr == null ? 0 : Integer.parseInt(maxStr);

            if (used >= max) {
                callback.onFailure("No washes remaining on this package");
                return;
            }

            int newUsed = used + 1;
            Map<String, Object> updates = new HashMap<>();
            updates.put("washesUsed", String.valueOf(newUsed));
            if (newUsed >= max) {
                updates.put("status", "completed");
            }

            ref.update(updates)
                    .addOnSuccessListener(v -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void saveWashReport(String customerName, String vehicle, String packageName, String cashierName, OperationCallback callback) {
        Map<String, Object> report = new HashMap<>();
        report.put("customerName", customerName);
        report.put("vehicle", vehicle);
        report.put("packageName", packageName);
        report.put("cashierName", cashierName);
        report.put("timestamp", FieldValue.serverTimestamp());

        db.collection("WashReports").add(report)
                .addOnSuccessListener(docRef -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
