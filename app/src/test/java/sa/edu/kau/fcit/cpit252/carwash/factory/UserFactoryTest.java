package sa.edu.kau.fcit.cpit252.carwash.factory;

import org.junit.Test;

import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.Owner;
import sa.edu.kau.fcit.cpit252.carwash.models.User;

import static org.junit.Assert.*;

public class UserFactoryTest {

    @Test
    public void createUser_shouldCreateCustomerWithCorrectData() {
        User user = UserFactory.createUser("CUSTOMER", "Ali", "Ahmed", "ali@example.com", "123456");

        assertTrue(user instanceof Customer);
        assertEquals("CUSTOMER", user.getRole());
        assertEquals("Ali Ahmed", user.getFullName());
        assertEquals("ali@example.com", user.getEmail());
        assertEquals("123456", user.getPassword());
    }

    @Test
    public void createUser_shouldCreateCashierWithCorrectData() {
        User user = UserFactory.createUser("CASHIER", "Sara", "Omar", "sara@example.com", "abcdef");

        assertTrue(user instanceof Cashier);
        assertEquals("CASHIER", user.getRole());
        assertEquals("Sara Omar", user.getFullName());
    }

    @Test
    public void createUser_shouldCreateOwnerWithCorrectData() {
        User user = UserFactory.createUser("OWNER", "Khaled", "Saleh", "owner@example.com", "abcdef");

        assertTrue(user instanceof Owner);
        assertEquals("OWNER", user.getRole());
        assertEquals("Khaled Saleh", user.getFullName());
    }

    @Test
    public void createUser_shouldAcceptDifferentLetterCases() {
        assertTrue(UserFactory.createUser("customer", "A", "B", "c@example.com", "123456") instanceof Customer);
        assertTrue(UserFactory.createUser("cashier", "A", "B", "c@example.com", "123456") instanceof Cashier);
        assertTrue(UserFactory.createUser("owner", "A", "B", "c@example.com", "123456") instanceof Owner);
    }

    @Test
    public void createUser_shouldReturnNullForInvalidOrNullType() {
        assertNull(UserFactory.createUser("ADMIN", "A", "B", "admin@example.com", "123456"));
        assertNull(UserFactory.createUser(null, "A", "B", "null@example.com", "123456"));
    }

    @Test
    public void createUser_shouldCreateDifferentObjectsForDifferentCalls() {
        User first = UserFactory.createUser("CUSTOMER", "Ali", "Ahmed", "ali@example.com", "123456");
        User second = UserFactory.createUser("CUSTOMER", "Ali", "Ahmed", "ali@example.com", "123456");

        assertNotSame("Factory should create a new object each time", first, second);
        assertEquals(first.getFullName(), second.getFullName());
        assertEquals(first.getRole(), second.getRole());
    }
}
