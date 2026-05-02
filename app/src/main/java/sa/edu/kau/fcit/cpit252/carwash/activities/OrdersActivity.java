package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import sa.edu.kau.fcit.cpit252.carwash.R;

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            orderListener = db.collection("Orders").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException error) {
                    if (error != null) {
                        Toast.makeText(OrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        tvNoOrdersMessage.setVisibility(View.GONE);
                        cvOrderCard.setVisibility(View.VISIBLE);

                        String packageName = snapshot.getString("packageName");
                        String packagePrice = snapshot.getString("packagePrice");
                        String vehicle = snapshot.getString("vehicle");
                        String washesStr = snapshot.getString("washesUsed");
                        String maxWashesStr = snapshot.getString("maxWashes");
                        String daysLeft = snapshot.getString("daysLeft");

                        tvOrderName.setText(packageName);
                        tvOrderPrice.setText(packagePrice);
                        tvCarInfo.setText("Vehicle: " + vehicle);
                        tvWashCount.setText("Washes used: " + washesStr + " of " + maxWashesStr);
                        tvDaysLeft.setText("Expires in " + daysLeft + " days");

                        if (washesStr != null) {
                            pbWashes.setProgress(Integer.parseInt(washesStr));
                        }

                    } else {
                        cvOrderCard.setVisibility(View.GONE);
                        tvNoOrdersMessage.setVisibility(View.VISIBLE);
                    }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
