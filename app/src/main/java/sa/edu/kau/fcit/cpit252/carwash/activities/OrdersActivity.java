package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sa.edu.kau.fcit.cpit252.carwash.R;

public class OrdersActivity extends AppCompatActivity {
    private Button btnCustomerScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        btnCustomerScreen = findViewById(R.id.btnBackToHome);


        btnCustomerScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersActivity.this, CustomerActivity.class);
                startActivity(intent);
            }
        });


    }
}
