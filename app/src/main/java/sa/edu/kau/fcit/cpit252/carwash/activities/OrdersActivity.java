package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;

public class OrdersActivity extends AppCompatActivity {
    private Button btnBackToHome;
    private CardView cvOrderCard;
    private TextView tvOrderName;
    private TextView tvOrderPrice;
    private TextView tvCarInfo;
    private TextView tvWashCount;
    private TextView tvDaysLeft;
    private TextView tvNoOrdersMessage;
    private ProgressBar pbWashes;
    private ImageView ivOrderQr;
    private TextView tvOrderId;
    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        btnBackToHome = findViewById(R.id.btnBackToHome);
        cvOrderCard = findViewById(R.id.cvOrderCard);
        tvNoOrdersMessage = findViewById(R.id.tvNoOrdersMessage);
        tvOrderName = findViewById(R.id.tvOrderName);
        tvOrderPrice = findViewById(R.id.tvOrderPrice);
        tvCarInfo = findViewById(R.id.tvCarInfo);
        tvWashCount = findViewById(R.id.tvWashCount);
        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        pbWashes = findViewById(R.id.pbWashes);
        ivOrderQr = findViewById(R.id.ivOrderQr);
        tvOrderId = findViewById(R.id.tvOrderId);

        FirebaseUser currentUser = DatabaseManager.getInstance().getAuth().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            orderListener = DatabaseManager.getInstance().getDb()
                    .collection("Orders")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Toast.makeText(OrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot activeOrder = null;
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                if ("active".equals(doc.getString("status"))) {
                                    activeOrder = doc;
                                    break;
                                }
                            }
                        }

                        if (activeOrder != null) {
                            tvNoOrdersMessage.setVisibility(View.GONE);
                            cvOrderCard.setVisibility(View.VISIBLE);

                            String packageName = activeOrder.getString("packageName");
                            String packagePrice = activeOrder.getString("packagePrice");
                            String vehicle = activeOrder.getString("vehicle");
                            String washesStr = activeOrder.getString("washesUsed");
                            String maxWashesStr = activeOrder.getString("maxWashes");
                            String daysLeft = activeOrder.getString("daysLeft");
                            String shortCode = activeOrder.getString("shortCode");

                            tvOrderName.setText(packageName);
                            tvOrderPrice.setText(packagePrice);
                            tvCarInfo.setText("Vehicle: " + vehicle);
                            tvWashCount.setText("Washes used: " + washesStr + " of " + maxWashesStr);
                            refreshDaysLeft(activeOrder);
                            tvOrderId.setText("Order ID: " + shortCode);

                            if (washesStr != null) {
                                pbWashes.setProgress(Integer.parseInt(washesStr));
                            }

                            generateQrCode(shortCode);

                        } else {
                            cvOrderCard.setVisibility(View.GONE);
                            tvNoOrdersMessage.setVisibility(View.VISIBLE);
                        }
                    });
        }

        btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersActivity.this, CustomerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void generateQrCode(String orderId) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(orderId, BarcodeFormat.QR_CODE, 500, 500);
            ivOrderQr.setImageBitmap(bitmap);
            android.util.Log.d("OrdersActivity", "QR generated for " + orderId);
        } catch (Throwable t) {
            android.util.Log.e("OrdersActivity", "QR generation failed", t);
            Toast.makeText(this, "QR error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static final int VALIDITY_DAYS = 30;

    private void refreshDaysLeft(DocumentSnapshot order) {
        com.google.firebase.Timestamp createdAt = order.getTimestamp("createdAt");
        if (createdAt == null) {
            tvDaysLeft.setText("Expires in " + VALIDITY_DAYS + " days");
            return;
        }

        long elapsedMs = System.currentTimeMillis() - createdAt.toDate().getTime();
        long elapsedDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(elapsedMs);
        long daysLeft = Math.max(0, VALIDITY_DAYS - elapsedDays);

        tvDaysLeft.setText("Expires in " + daysLeft + " days");

        String storedDaysLeft = order.getString("daysLeft");
        String newDaysLeft = String.valueOf(daysLeft);
        if (!newDaysLeft.equals(storedDaysLeft)) {
            order.getReference().update("daysLeft", newDaysLeft);
        }

        if (daysLeft == 0 && "active".equals(order.getString("status"))) {
            order.getReference().update("status", "expired");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
