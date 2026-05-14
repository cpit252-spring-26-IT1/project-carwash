package sa.edu.kau.fcit.cpit252.carwash.activities;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.adapters.StaffAdapter;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.User;
public class StaffManagementActivity extends AppCompatActivity {
    private TextView tvBack;
    private Button btnAddCashier;
    private ListView listStaff;
    private TextView tvEmpty;

    private final List<String> cashierIds = new ArrayList<>();
    private final List<Cashier> cashiers = new ArrayList<>();
    private StaffAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        tvBack = findViewById(R.id.tvBackStaff);
        btnAddCashier = findViewById(R.id.btnAddCashier);
        listStaff = findViewById(R.id.listStaff);
        tvEmpty = findViewById(R.id.tvEmptyStaff);

        adapter = new StaffAdapter(this, cashierIds, cashiers, new StaffAdapter.OnDeleteListener() {
            @Override
            public void onDelete(String userId, Cashier cashier) {
                confirmDelete(userId, cashier);
            }
        });
        listStaff.setAdapter(adapter);

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnAddCashier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StaffManagementActivity.this, AddCashierActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCashiers();
    }

    private void loadCashiers() {
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();
        db.collection("Users")
                .whereEqualTo("role", "CASHIER")
                .get()
                .addOnSuccessListener(querySnap -> {
                    cashierIds.clear();
                    cashiers.clear();
                    for (QueryDocumentSnapshot doc : querySnap) {
                        User user = UserFactory.createUser(
                                "CASHIER",
                                doc.getString("firstName"),
                                doc.getString("lastName"),
                                doc.getString("email"),
                                null
                        );
                        if (user instanceof Cashier) {
                            cashierIds.add(doc.getId());
                            cashiers.add((Cashier) user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(cashiers.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void confirmDelete(final String userId, final Cashier cashier) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Cashier")
                .setMessage("Remove " + cashier.getFullName() + " from staff?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCashier(userId);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deleteCashier(String userId) {
        FirebaseFirestore db = DatabaseManager.getInstance().getDb();
        db.collection("Users").document(userId).delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Cashier removed", Toast.LENGTH_SHORT).show();
                    loadCashiers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}