package sa.edu.kau.fcit.cpit252.carwash.models;

public class Cashier extends User {

    public Cashier(){
    }
    public Cashier(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }

    @Override
    public String getRole() {
        return "CASHIER";
    }
}
