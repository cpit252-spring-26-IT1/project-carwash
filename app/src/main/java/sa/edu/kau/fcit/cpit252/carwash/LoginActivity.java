package sa.edu.kau.fcit.cpit252.carwash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = DataStore.login(email, password);

                if (user != null) {
                    handleNavigation(user);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleNavigation(User user) {
        Intent intent;

        if (user instanceof Owner) {
            intent = new Intent(this, OwnerActivity.class);
        } else if (user instanceof Cashier) {
            intent = new Intent(this, CashierActivity.class);
        } else {
            intent = new Intent(this, CustomerActivity.class);
        }

        intent.putExtra("USER_NAME", user.getFirstName());
        startActivity(intent);
        finish();
    }
}