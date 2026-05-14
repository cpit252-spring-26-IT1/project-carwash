package sa.edu.kau.fcit.cpit252.carwash.models;

public class Customer extends User{
    private int loyaltyPoints;
    private boolean blackListed;

    public Customer(){
    }

    public Customer(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        this.loyaltyPoints = 0;
        this.blackListed = false;

    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public boolean isBlacklisted() {
        return blackListed;
    }
    public void setBlacklisted(boolean blacklisted) {
        this.blackListed = blacklisted;
    }


}

