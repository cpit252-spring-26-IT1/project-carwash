package sa.edu.kau.fcit.cpit252.carwash.models;

public class Customer extends User{
    private int loyaltyPoints;

    public Customer(){
    }

    public Customer(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        this.loyaltyPoints = 0;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }
}
