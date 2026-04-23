package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sa.edu.kau.fcit.cpit252.carwash.R;

public class OwnerActivity extends AppCompatActivity {

    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        tvWelcome = findViewById(R.id.tvWelcome);

        String userName = getIntent().getStringExtra("USER_NAME");

        if (userName != null) {
            tvWelcome.setText("Welcome, " + userName);
    }}
}