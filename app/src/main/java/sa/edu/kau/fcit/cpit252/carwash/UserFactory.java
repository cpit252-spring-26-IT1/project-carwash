package sa.edu.kau.fcit.cpit252.carwash;

public class UserFactory {

    public static User createUser(String type, String fName, String lName, String email, String password) {
        if (type == null) {
            return null;
        }

        if (type.equalsIgnoreCase("CUSTOMER")) {
            return new Customer(fName, lName, email, password);
        }
        else if (type.equalsIgnoreCase("CASHIER")) {
            return new Cashier(fName, lName, email, password);
        }
        else if (type.equalsIgnoreCase("OWNER")) {
            return new Owner(fName, lName, email, password);
        }


        return null;

    }}
