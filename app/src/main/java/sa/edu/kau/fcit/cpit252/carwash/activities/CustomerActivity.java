package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.bridge.*;


import android.graphics.Color;

public class CustomerActivity extends AppCompatActivity {
    private CardView cardFull, cardOutside, cardInside;
    private Spinner spinnerCarType;
    private TextView tvPriceFull, tvPriceOutside, tvPriceInside;
    private Button btnPurchase, btnLogout, btnMyOrders;
    private String selectedPrice = "";
    private String selectedPackage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        cardFull = findViewById(R.id.cardFull);
        cardOutside = findViewById(R.id.cardOutside);
        cardInside = findViewById(R.id.cardInside);
        spinnerCarType = findViewById(R.id.spinnerCarType);
        tvPriceFull = findViewById(R.id.tvPriceFull);
        tvPriceOutside = findViewById(R.id.tvPriceOutside);
        tvPriceInside = findViewById(R.id.tvPriceInside);
        btnPurchase = findViewById(R.id.btnPurchase);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnLogout = findViewById(R.id.btnLogout);

        String[] cars = {"Sedan", "SUV", "Crossover"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cars);
        spinnerCarType.setAdapter(adapter);

        spinnerCarType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePrices(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cardFull.setOnClickListener(v -> {
            selectedPrice = tvPriceFull.getText().toString();
            selectedPackage = "Full Service";
            highlightCard(cardFull);
        });

        cardOutside.setOnClickListener(v -> {
            selectedPrice = tvPriceOutside.getText().toString();
            selectedPackage = "Exterior Only";
            highlightCard(cardOutside);
        });

        cardInside.setOnClickListener(v -> {
            selectedPrice = tvPriceInside.getText().toString();
            selectedPackage = "Interior Only";
            highlightCard(cardInside);
        });

        btnPurchase.setOnClickListener(v -> {
            if (selectedPackage.isEmpty()) {
                Toast.makeText(CustomerActivity.this, "Please select a package first!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CustomerActivity.this, PaymentActivity.class);
                intent.putExtra("price", selectedPrice);
                intent.putExtra("package_name", selectedPackage);
                startActivity(intent);
            }
        });

        btnMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CustomerActivity.this).setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(android.content.DialogInterface dialog, int which) {
                                Intent intent = new Intent(CustomerActivity.this, MainActivity.class);
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
        }
        );
    }

    private void updatePrices(int carPosition) {
        VehiclePricing pricing;
        if (carPosition == 0)
            pricing = new SedanPricing();

        else if (carPosition == 1)
            pricing = new SUVPricing();
        else
            pricing = new CrossoverPricing();

        WashPackage full = new FullServicePackage(pricing);
        WashPackage exterior = new ExteriorPackage(pricing);
        WashPackage interior = new InteriorPackage(pricing);

        tvPriceFull.setText("SAR " + full.getPrice());
        tvPriceOutside.setText("SAR " + exterior.getPrice());
        tvPriceInside.setText("SAR " + interior.getPrice());
    }


    private void highlightCard(CardView selected) {
        cardFull.setCardBackgroundColor(Color.WHITE);
        cardOutside.setCardBackgroundColor(Color.WHITE);
        cardInside.setCardBackgroundColor(Color.WHITE);
        cardFull.setCardElevation(4f);
        cardOutside.setCardElevation(4f);
        cardInside.setCardElevation(4f);
        selected.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        selected.setCardElevation(15f);
    }
}