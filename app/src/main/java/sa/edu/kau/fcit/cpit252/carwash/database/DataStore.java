package sa.edu.kau.fcit.cpit252.carwash.database;

import sa.edu.kau.fcit.cpit252.carwash.models.User;

public class DataStore {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logoutUser() {
        currentUser = null;
        DatabaseManager.getInstance().getAuth().signOut();
    }
}