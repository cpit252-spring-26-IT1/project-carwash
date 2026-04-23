package sa.edu.kau.fcit.cpit252.carwash.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseManager {

    private static DatabaseManager instance;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private DatabaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }
    public FirebaseFirestore getDb() {
        return db;
    }
}
