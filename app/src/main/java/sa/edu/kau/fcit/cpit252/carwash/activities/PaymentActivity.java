package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.database.OrderManager;

public class PaymentActivity extends AppCompatActivity {
    private EditText etCardNumber;
    private EditText etExpiryDate;
    private EditText etCVV;
    private Button btnConfirmPurchase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TextView tvBackPayment = findViewById(R.id.tvBackPayment);
        tvBackPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);
        btnConfirmPurchase = findViewById(R.id.btnConfirmPur);

        btnConfirmPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber = etCardNumber.getText().toString().trim();
                String expiryDate = etExpiryDate.getText().toString().trim();
                String cvv = etCVV.getText().toString().trim();

                if(cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
                    Toast.makeText(PaymentActivity.this, "Please fill in all payment details", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!expiryDate.contains("/")) {
                    Toast.makeText(PaymentActivity.this, "Please include the '/' (e.g., 02/26)", Toast.LENGTH_LONG).show();
                    return;
                }

                int month = Integer.parseInt(expiryDate.substring(0, 2));

                if (month < 1 || month > 12) {
                    Toast.makeText(PaymentActivity.this, "Invalid month! Must be between 01 and 12.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String purchasedPackage = getIntent().getStringExtra("package_name");
                String purchasedPrice = getIntent().getStringExtra("price");
                String selectedVehicle = getIntent().getStringExtra("vehicle");

                if (purchasedPackage == null)
                    purchasedPackage = "Unknown Package";

                if (purchasedPrice == null)
                    purchasedPrice = "SAR 0";

                if (selectedVehicle == null)
                    selectedVehicle = "Unknown";

                String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

                OrderManager manager = new OrderManager();

                manager.saveNewOrder(userId, purchasedPackage, purchasedPrice, selectedVehicle, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(PaymentActivity.this, SuccessActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Error saving order", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}