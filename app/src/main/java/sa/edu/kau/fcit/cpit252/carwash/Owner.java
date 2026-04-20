package sa.edu.kau.fcit.cpit252.carwash;

public class Owner extends User {
    public Owner(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }

    @Override
    public String getRole() {
        return "OWNER";
    }
}
