package sa.edu.kau.fcit.cpit252.carwash;

public class Customer extends User{
    private int loyaltyPoints;

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
