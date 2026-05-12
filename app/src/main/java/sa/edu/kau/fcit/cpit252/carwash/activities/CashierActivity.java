package sa.edu.kau.fcit.cpit252.carwash.activities;


import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import sa.edu.kau.fcit.cpit252.carwash.R;

public class CashierActivity extends AppCompatActivity {

    private TextView tvLoggedInCashier;
    private EditText etSearchInput;
    private ImageButton btnScanQR;
    private Button btnSearch;
    private Button btnConfirmWash;


    private LinearLayout layoutCustomerDetails;
    private TextView tvCustomerName;
    private TextView tvCarType;
    private TextView tvPackageType;
    private TextView tvBalanceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);


        tvLoggedInCashier = findViewById(R.id.tvLoggedInCashier);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnSearch = findViewById(R.id.btnSearch);
        btnConfirmWash = findViewById(R.id.btnConfirmWash);


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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btnScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btnConfirmWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}