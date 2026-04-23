package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.models.User;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;

public class SignUpActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etEmail, etPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName = etFirstName.getText().toString().trim();
                String lName = etLastName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth mAuth = DatabaseManager.getInstance().getAuth();
                FirebaseFirestore db = DatabaseManager.getInstance().getDb();

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();
                                String role = "CUSTOMER";

                                User newUser = UserFactory.createUser(role, fName, lName, email, pass);

                                db.collection("Users").document(userId).set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            db.collection("Users").document(userId).update("role", role);

                                            DataStore.setCurrentUser(newUser);

                                            Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                            } else {
                                Toast.makeText(SignUpActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}