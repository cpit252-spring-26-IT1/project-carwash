package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;
import sa.edu.kau.fcit.cpit252.carwash.models.User;

public class AddCashierActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etEmail, etPassword;
    private Button btnSave;
    private TextView tvBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cashier);

        etFirstName = findViewById(R.id.etCashierFirstName);
        etLastName = findViewById(R.id.etCashierLastName);
        etEmail = findViewById(R.id.etCashierEmail);
        etPassword = findViewById(R.id.etCashierPassword);
        btnSave = findViewById(R.id.btnSaveCashier);
        tvBack = findViewById(R.id.tvBackAddCashier);

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCashier();
            }
        });
    }

    private void createCashier() {
        String fName = etFirstName.getText().toString().trim();
        String lName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fName.matches("^[a-zA-Z]+$") || !lName.matches("^[a-zA-Z]+$")) {
            Toast.makeText(this, "Names can only contain letters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        final User owner = DataStore.getCurrentUser();
        if (owner == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }
        final String ownerEmail = owner.getEmail();
        final String ownerPassword = owner.getPassword();

        btnSave.setEnabled(false);

        FirebaseAuth mAuth = DatabaseManager.getInstance().getAuth();
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    String cashierUid = authResult.getUser().getUid();

                    User newCashier = UserFactory.createUser("CASHIER", fName, lName, email, pass);

                    db.collection("Users").document(cashierUid).set(newCashier)
                            .addOnSuccessListener(aVoid -> {
                                db.collection("Users").document(cashierUid).update("role", "CASHIER");

                                mAuth.signOut();
                                mAuth.signInWithEmailAndPassword(ownerEmail, ownerPassword)
                                        .addOnCompleteListener(t -> {
                                            Toast.makeText(AddCashierActivity.this,
                                                    "Cashier created", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddCashierActivity.this,
                                        "Failed to save cashier: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                btnSave.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddCashierActivity.this,
                            "Failed to create account: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnSave.setEnabled(true);
                });
    }
}