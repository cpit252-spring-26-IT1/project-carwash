package sa.edu.kau.fcit.cpit252.carwash.database;

import android.media.MediaPlayer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;

public class OrderManager {

    private final FirebaseFirestore db;

    public OrderManager() {
        this.db = DatabaseManager.getInstance().getDb();
    }
    public static void saveNewOrder(String userId, String packageName, String packagePrice, String vehicle, OnCompleteListener<Void> completionListener) {

        HashMap<String, String> orderDetails = new HashMap<>();
        orderDetails.put("packageName", packageName);
        orderDetails.put("packagePrice", packagePrice);
        orderDetails.put("vehicle", vehicle);
        orderDetails.put("washesUsed", "0");
        orderDetails.put("maxWashes", "5");
        orderDetails.put("daysLeft", "30");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Orders").document(userId).set(orderDetails).addOnCompleteListener(completionListener);
    }
}
