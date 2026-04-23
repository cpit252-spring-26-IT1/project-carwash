package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.models.*;
import sa.edu.kau.fcit.cpit252.carwash.database.*;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
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

                FirebaseAuth mAuth = DatabaseManager.getInstance().getAuth();
                FirebaseFirestore db = DatabaseManager.getInstance().getDb();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();

                                db.collection("Users").document(userId).get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                String role = document.getString("role");
                                                String fName = document.getString("firstName");
                                                String lName = document.getString("lastName");
                                                String userEmail = document.getString("email");

                                                User loggedInUser = UserFactory.createUser(role, fName, lName, userEmail, password);

                                                DataStore.setCurrentUser(loggedInUser);

                                                handleNavigation(loggedInUser);
                                            } else {
                                                Toast.makeText(LoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void handleNavigation(User user) {
        if (user == null) return;

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