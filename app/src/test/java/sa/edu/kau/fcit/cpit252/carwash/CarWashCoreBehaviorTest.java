package sa.edu.kau.fcit.cpit252.carwash;

import org.junit.Test;

import sa.edu.kau.fcit.cpit252.carwash.bridge.CrossoverPricing;
import sa.edu.kau.fcit.cpit252.carwash.bridge.ExteriorPackage;
import sa.edu.kau.fcit.cpit252.carwash.bridge.FullServicePackage;
import sa.edu.kau.fcit.cpit252.carwash.bridge.InteriorPackage;
import sa.edu.kau.fcit.cpit252.carwash.bridge.SUVPricing;
import sa.edu.kau.fcit.cpit252.carwash.bridge.SedanPricing;
import sa.edu.kau.fcit.cpit252.carwash.bridge.VehiclePricing;
import sa.edu.kau.fcit.cpit252.carwash.bridge.WashPackage;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.Owner;
import sa.edu.kau.fcit.cpit252.carwash.models.User;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventBus;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CarWashCoreBehaviorTest {

    private static final double DELTA = 0.0001;

    @Test
    public void customerSignupFlow_shouldCreateCustomerAndStoreSession() {
        User user = UserFactory.createUser("CUSTOMER", "Ali", "Saleh", "customer@example.com", "123456");

        DataStore.setCurrentUser(user);

        assertTrue(user instanceof Customer);
        assertEquals("CUSTOMER", DataStore.getCurrentUser().getRole());
        assertEquals("Ali Saleh", DataStore.getCurrentUser().getFullName());
        assertEquals("customer@example.com", DataStore.getCurrentUser().getEmail());
        assertSame(user, DataStore.getCurrentUser());
    }

    @Test
    public void roleBasedLoginFlow_shouldCreateCorrectUserTypeForEachRole() {
        User customer = UserFactory.createUser("CUSTOMER", "A", "B", "c@example.com", "123456");
        User cashier = UserFactory.createUser("CASHIER", "C", "D", "cashier@example.com", "123456");
        User owner = UserFactory.createUser("OWNER", "E", "F", "owner@example.com", "123456");

        assertTrue(customer instanceof Customer);
        assertTrue(cashier instanceof Cashier);
        assertTrue(owner instanceof Owner);
        assertEquals("CUSTOMER", customer.getRole());
        assertEquals("CASHIER", cashier.getRole());
        assertEquals("OWNER", owner.getRole());
    }

    @Test
    public void packageSelectionFlow_shouldCalculateAllDisplayedPricesCorrectly() {
        assertPackagePrices(new SedanPricing(), 200.0, 100.0, 80.0);
        assertPackagePrices(new SUVPricing(), 300.0, 150.0, 120.0);
        assertPackagePrices(new CrossoverPricing(), 260.0, 130.0, 104.0);
    }

    @Test
    public void selectingDifferentVehiclePricing_shouldChangePriceWithoutChangingPackageClass() {
        WashPackage sedanFull = new FullServicePackage(new SedanPricing());
        WashPackage suvFull = new FullServicePackage(new SUVPricing());

        assertEquals("Full Service", sedanFull.getPackageName());
        assertEquals("Full Service", suvFull.getPackageName());
        assertEquals(200.0, sedanFull.getPrice(), DELTA);
        assertEquals(300.0, suvFull.getPrice(), DELTA);
    }

    @Test
    public void customerBlacklistFlow_shouldChangeCustomerStatus() {
        Customer customer = new Customer("Sara", "Omar", "sara@example.com", "123456");

        assertFalse(customer.isBlacklisted());
        customer.setBlacklisted(true);
        assertTrue(customer.isBlacklisted());
        customer.setBlacklisted(false);
        assertFalse(customer.isBlacklisted());
    }

    @Test
    public void washEventFlow_shouldNotifySubscribedScreensWithSameOrderData() {
        WashEventBus bus = WashEventBus.getInstance();
        RecordingScreen ordersScreen = new RecordingScreen();
        RecordingScreen analyticsScreen = new RecordingScreen();

        bus.unsubscribe(ordersScreen);
        bus.unsubscribe(analyticsScreen);
        bus.subscribe(ordersScreen);
        bus.subscribe(analyticsScreen);

        bus.publishWashDeducted("ORDER123", "Ali Saleh", "Exterior Only");
        bus.publishOrderCompleted("ORDER123");

        bus.unsubscribe(ordersScreen);
        bus.unsubscribe(analyticsScreen);

        assertEquals(1, ordersScreen.washDeductedCount);
        assertEquals(1, analyticsScreen.washDeductedCount);
        assertEquals(1, ordersScreen.completedCount);
        assertEquals(1, analyticsScreen.completedCount);
        assertEquals("ORDER123", ordersScreen.lastOrderId);
        assertEquals("Ali Saleh", ordersScreen.lastCustomerName);
        assertEquals("Exterior Only", ordersScreen.lastPackageName);
    }

    @Test
    public void factoryFlow_shouldCreateNewIndependentObjectsEachTime() {
        User first = UserFactory.createUser("CUSTOMER", "Ali", "Saleh", "a@example.com", "123456");
        User second = UserFactory.createUser("CUSTOMER", "Ali", "Saleh", "a@example.com", "123456");

        assertNotSame(first, second);
        assertEquals(first.getFullName(), second.getFullName());
        assertEquals(first.getRole(), second.getRole());
    }

    private void assertPackagePrices(VehiclePricing pricing, double full, double exterior, double interior) {
        assertEquals(full, new FullServicePackage(pricing).getPrice(), DELTA);
        assertEquals(exterior, new ExteriorPackage(pricing).getPrice(), DELTA);
        assertEquals(interior, new InteriorPackage(pricing).getPrice(), DELTA);
    }

    private static class RecordingScreen implements WashEventListener {
        int washDeductedCount;
        int completedCount;
        String lastOrderId;
        String lastCustomerName;
        String lastPackageName;

        @Override
        public void onWashDeducted(String orderId, String customerName, String packageName) {
            washDeductedCount++;
            lastOrderId = orderId;
            lastCustomerName = customerName;
            lastPackageName = packageName;
        }

        @Override
        public void onOrderCompleted(String orderId) {
            completedCount++;
            lastOrderId = orderId;
        }
    }
}
