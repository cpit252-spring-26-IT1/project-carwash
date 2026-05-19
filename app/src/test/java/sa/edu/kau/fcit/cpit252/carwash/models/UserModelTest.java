package sa.edu.kau.fcit.cpit252.carwash.models;

import com.google.firebase.firestore.Exclude;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;

import static org.junit.Assert.*;

public class UserModelTest {

    @Test
    public void customerConstructor_shouldStoreUserDataAndDefaultCustomerValues() {
        Customer customer = new Customer("Ali", "Ahmed", "ali@example.com", "123456");

        assertEquals("Ali", customer.getFirstName());
        assertEquals("Ahmed", customer.getLastName());
        assertEquals("ali@example.com", customer.getEmail());
        assertEquals("123456", customer.getPassword());
        assertEquals("Ali Ahmed", customer.getFullName());
        assertEquals("CUSTOMER", customer.getRole());
        assertEquals(0, customer.getLoyaltyPoints());
        assertFalse(customer.isBlacklisted());
    }

    @Test
    public void cashierAndOwner_shouldReturnCorrectRolesAndFullNames() {
        Cashier cashier = new Cashier("Sara", "Saleh", "cashier@example.com", "abcdef");
        Owner owner = new Owner("Omar", "Khaled", "owner@example.com", "abcdef");

        assertEquals("CASHIER", cashier.getRole());
        assertEquals("Sara Saleh", cashier.getFullName());

        assertEquals("OWNER", owner.getRole());
        assertEquals("Omar Khaled", owner.getFullName());
    }

    @Test
    public void customerBlacklist_shouldToggleCorrectly() {
        Customer customer = new Customer("Nora", "Ali", "nora@example.com", "123456");

        assertFalse(customer.isBlacklisted());
        customer.setBlacklisted(true);
        assertTrue(customer.isBlacklisted());
        customer.setBlacklisted(false);
        assertFalse(customer.isBlacklisted());
    }

    @Test
    public void userClass_shouldBeAbstractAndPasswordShouldNotBeSavedToFirestore() throws Exception {
        assertTrue("User should be abstract", Modifier.isAbstract(User.class.getModifiers()));

        Method getPassword = User.class.getDeclaredMethod("getPassword");
        assertNotNull("getPassword() should have @Exclude so Firebase does not store passwords", getPassword.getAnnotation(Exclude.class));
    }

    @Test
    public void dataStore_shouldStoreAndReturnCurrentUserWithoutChangingObject() {
        Customer customer = new Customer("Fahad", "Saad", "fahad@example.com", "123456");

        DataStore.setCurrentUser(customer);

        assertSame(customer, DataStore.getCurrentUser());
        assertEquals("CUSTOMER", DataStore.getCurrentUser().getRole());

        DataStore.setCurrentUser(null);
        assertNull(DataStore.getCurrentUser());
    }
}
