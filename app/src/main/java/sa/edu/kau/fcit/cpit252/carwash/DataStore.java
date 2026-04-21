package sa.edu.kau.fcit.cpit252.carwash;

import java.util.HashMap;
import java.util.Map;

public class DataStore {

    public static Map<String, User> usersMap = new HashMap<>();

    public static void initData() {
        if (!usersMap.isEmpty()) {
            return;
        }

        User admin = UserFactory.createUser("OWNER", "Mohammed", "Ahmed", "mohammed@mail.com", "123");
        User cashier = UserFactory.createUser("CASHIER", "Ali", "Saleh", "ali_s@mail.com", "123");
        User customer = UserFactory.createUser("CUSTOMER", "Khaled", "Fahad", "khaled@mail.com", "123");

        if (admin != null){
            usersMap.put(admin.getEmail(), admin);
        }
        if (cashier != null){
            usersMap.put(cashier.getEmail(), cashier);
        }
        if (customer != null){
             usersMap.put(customer.getEmail(), customer);
    }}

    public static User login(String email, String password) {
        User user = usersMap.get(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public static void addUser(User newUser) {
        if (newUser != null) {
            usersMap.put(newUser.getEmail(), newUser);
        }
    }
}