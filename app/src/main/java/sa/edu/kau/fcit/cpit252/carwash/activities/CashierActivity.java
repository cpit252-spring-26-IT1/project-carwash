package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.database.OrderManager;
import sa.edu.kau.fcit.cpit252.carwash.models.User;

public class CashierActivity extends AppCompatActivity {

    private TextView tvLoggedInCashier;
    private EditText etSearchInput;
    private ImageButton btnScanQR;
    private Button btnSearch;
    private Button btnConfirmWash;
    private Button btnLogoutCashier;


    private LinearLayout layoutCustomerDetails;
    private TextView tvCustomerName;
    private TextView tvCarType;
    private TextView tvPackageType;
    private TextView tvBalanceStatus;

    private String currentOrderId;
    private String currentCustomerName;
    private String currentVehicle;
    private String currentPackage;

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    etSearchInput.setText(result.getContents());
                    performSearch(result.getContents());
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);


        tvLoggedInCashier = findViewById(R.id.tvLoggedInCashier);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnSearch = findViewById(R.id.btnSearch);
        btnConfirmWash = findViewById(R.id.btnConfirmWash);
        btnLogoutCashier = findViewById(R.id.btnLogoutCashier);


        layoutCustomerDetails = findViewById(R.id.layoutCustomerDetails);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCarType = findViewById(R.id.tvCarType);
        tvPackageType = findViewById(R.id.tvPackageType);
        tvBalanceStatus = findViewById(R.id.tvBalanceStatus);


        String userName = getIntent().getStringExtra("USER_NAME");
        if (userName != null) {
            tvLoggedInCashier.setText("Logged in as: " + userName);
        }
        setupListeners();
    }

    private void setupListeners() {

        btnSearch.setOnClickListener(v -> {
            String input = etSearchInput.getText().toString().trim();
            performSearch(input);
        });

        btnScanQR.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan customer's order QR");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(PortraitCaptureActivity.class);
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            qrLauncher.launch(options);
        });

        btnConfirmWash.setOnClickListener(v -> {
            if (currentOrderId == null) {
                Toast.makeText(this, "Look up a customer first", Toast.LENGTH_SHORT).show();
                return;
            }
            deductWashAndReport();
        });

        btnLogoutCashier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CashierActivity.this).setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                DataStore.logoutUser();
                                Intent intent = new Intent(CashierActivity.this, MainActivity.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void performSearch(String input) {
        if (input.isEmpty()) {
            Toast.makeText(this, "Enter an Order ID or scan a QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = input.trim().toUpperCase();
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();

        db.collection("Orders")
                .whereEqualTo("shortCode", code)
                .get()
                .addOnSuccessListener(querySnap -> {
                    com.google.firebase.firestore.DocumentSnapshot orderSnap = null;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnap.getDocuments()) {
                        if ("active".equals(doc.getString("status"))) {
                            orderSnap = doc;
                            break;
                        }
                    }

                    if (orderSnap == null) {
                        Toast.makeText(this, "No active order found for ID: " + code, Toast.LENGTH_SHORT).show();
                        clearCustomerCard();
                        return;
                    }

                    currentOrderId = orderSnap.getId();
                    currentPackage = orderSnap.getString("packageName");
                    currentVehicle = orderSnap.getString("vehicle");
                    String customerUserId = orderSnap.getString("userId");

                    String usedStr = orderSnap.getString("washesUsed");
                    String maxStr = orderSnap.getString("maxWashes");
                    int used = usedStr == null ? 0 : Integer.parseInt(usedStr);
                    int max = maxStr == null ? 0 : Integer.parseInt(maxStr);
                    int remaining = Math.max(0, max - used);

                    db.collection("Users").document(customerUserId).get()
                            .addOnSuccessListener(userSnap -> {
                                String fName = userSnap.getString("firstName");
                                String lName = userSnap.getString("lastName");
                                currentCustomerName = ((fName == null ? "" : fName) + " "
                                        + (lName == null ? "" : lName)).trim();
                                if (currentCustomerName.isEmpty()) currentCustomerName = "Customer";

                                tvCustomerName.setText("Customer: " + currentCustomerName);
                                tvCarType.setText("Car Type: " + (currentVehicle == null ? "--" : currentVehicle));
                                tvPackageType.setText("Package: " + (currentPackage == null ? "--" : currentPackage));
                                tvBalanceStatus.setText("Remaining: " + remaining + " Washes");

                                btnConfirmWash.setEnabled(remaining > 0);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lookup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deductWashAndReport() {
        btnConfirmWash.setEnabled(false);
        OrderManager manager = new OrderManager();

        manager.deductWash(currentOrderId, new OrderManager.OperationCallback() {
            @Override
            public void onSuccess() {
                User cashier = DataStore.getCurrentUser();
                String cashierName = (cashier == null) ? "Unknown" : cashier.getFullName();

                manager.saveWashReport(currentCustomerName, currentVehicle, currentPackage,
                        cashierName, new OrderManager.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                showCompletionDialog();
                            }
                            @Override
                            public void onFailure(String reason) {
                                Toast.makeText(CashierActivity.this,
                                        "Wash deducted but report failed: " + reason,
                                        Toast.LENGTH_LONG).show();
                                showCompletionDialog();
                            }
                        });
            }
            @Override
            public void onFailure(String reason) {
                Toast.makeText(CashierActivity.this, reason, Toast.LENGTH_LONG).show();
                btnConfirmWash.setEnabled(true);
            }
        });
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(CashierActivity.this)
                .setTitle("Wash Completed")
                .setMessage("A wash has been deducted from " + currentCustomerName + ".")
                .setIcon(R.drawable.baseline_check_24)
                .setPositiveButton("OK", (d, w) -> {
                    performSearch(etSearchInput.getText().toString());
                })
                .setCancelable(false)
                .show();
    }

    private void clearCustomerCard() {
        currentOrderId = null;
        currentCustomerName = null;
        currentVehicle = null;
        currentPackage = null;
        tvCustomerName.setText("Customer: --");
        tvCarType.setText("Car Type: --");
        tvPackageType.setText("Package: --");
        tvBalanceStatus.setText("Remaining: 0 Washes");
        btnConfirmWash.setEnabled(false);
    }
}