package sa.edu.kau.fcit.cpit252.carwash.models;

public class Owner extends User {

    public Owner(){
    }
    public Owner(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }

    @Override
    public String getRole() {
        return "OWNER";
    }
}
