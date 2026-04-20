package sa.edu.kau.fcit.cpit252.carwash;

import java.util.HashMap;
import java.util.Map;

public class DataStore {

    public static Map<String, User> usersMap = new HashMap<>();

    public static void initData() {
        if (!usersMap.isEmpty()) {
            return;
        }

        User admin = new Owner("Mohammed", "Ahmed", "mohammed@mail.com", "123");
        User cashier = new Cashier("Ali", "Saleh", "ali_s@mail.com", "123");
        User khaled = new Customer("Khaled", "Fahad", "khaled@mail.com", "123");

        usersMap.put(admin.getEmail(), admin);
        usersMap.put(cashier.getEmail(), cashier);
        usersMap.put(khaled.getEmail(), khaled);
    }

    public static User login(String email, String password) {
        User user = usersMap.get(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public static void addUser(User newUser) {
        usersMap.put(newUser.getEmail(), newUser);
    }
}
