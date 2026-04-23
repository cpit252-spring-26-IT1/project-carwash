package sa.edu.kau.fcit.cpit252.carwash.models;

public abstract class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public User(){
    }
    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public abstract String getRole();
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
