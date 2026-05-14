package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.adapters.CustomerAdapter;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.User;
public class CustomerBlacklistActivity extends AppCompatActivity {

    private TextView tvBack;
    private ListView listCustomers;
    private TextView tvEmpty;
    private EditText etSearch;

    private final List<String> customerIds = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private CustomerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_blacklist);

        tvBack = findViewById(R.id.tvBackBlacklist);
        listCustomers = findViewById(R.id.listCustomers);
        tvEmpty = findViewById(R.id.tvEmptyCustomers);
        etSearch = findViewById(R.id.etSearchCustomer);

        adapter = new CustomerAdapter(this, customerIds, customers,
                new CustomerAdapter.OnBlockToggleListener() {
                    @Override
                    public void onToggle(String userId, Customer customer) {
                        confirmToggle(userId, customer);
                    }
                });
        listCustomers.setAdapter(adapter);

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                tvEmpty.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers();
    }

    private void loadCustomers() {
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();
        db.collection("Users")
                .whereEqualTo("role", "CUSTOMER")
                .get()
                .addOnSuccessListener(querySnap -> {
                    customerIds.clear();
                    customers.clear();
                    for (QueryDocumentSnapshot doc : querySnap) {
                        User user = UserFactory.createUser(
                                "CUSTOMER",
                                doc.getString("firstName"),
                                doc.getString("lastName"),
                                doc.getString("email"),
                                null
                        );
                        if (user instanceof Customer) {
                            Customer c = (Customer) user;
                            // Firestore is the source of truth for the blacklist flag
                            Boolean blocked = doc.getBoolean("blacklisted");
                            c.setBlacklisted(blocked != null && blocked);

                            customerIds.add(doc.getId());
                            customers.add(c);
                        }
                    }
                    adapter.filter(etSearch.getText().toString());
                    tvEmpty.setVisibility(customers.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void confirmToggle(final String userId, final Customer customer) {
        final boolean willBlock = !customer.isBlacklisted();
        String action = willBlock ? "Block" : "Unblock";
        String message = willBlock
                ? "Block " + customer.getFullName() + "? They will no longer be able to book a wash."
                : "Unblock " + customer.getFullName() + "? They will regain access.";

        new AlertDialog.Builder(this)
                .setTitle(action + " Customer")
                .setMessage(message)
                .setPositiveButton(action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateBlacklist(userId, willBlock);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void updateBlacklist(String userId, boolean blacklisted) {
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();
        db.collection("Users").document(userId)
                .update("blacklisted", blacklisted)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            blacklisted ? "Customer blocked" : "Customer unblocked",
                            Toast.LENGTH_SHORT).show();
                    loadCustomers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
