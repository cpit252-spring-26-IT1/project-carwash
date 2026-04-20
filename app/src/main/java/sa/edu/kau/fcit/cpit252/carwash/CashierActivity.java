package sa.edu.kau.fcit.cpit252.carwash;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CashierActivity extends AppCompatActivity {


    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);

        tvWelcome = findViewById(R.id.tvWelcome);

        String userName = getIntent().getStringExtra("USER_NAME");

        if (userName != null) {
            tvWelcome.setText("Welcome Cashier, " + userName);
        }
    }
}