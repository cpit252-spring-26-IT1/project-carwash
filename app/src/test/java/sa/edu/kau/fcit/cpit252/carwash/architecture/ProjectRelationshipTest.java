package sa.edu.kau.fcit.cpit252.carwash.architecture;

import android.widget.BaseAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sa.edu.kau.fcit.cpit252.carwash.activities.AddCashierActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.AnalyticsActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CashierActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CustomerActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CustomerBlacklistActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.LoginActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.MainActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.OrdersActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.OwnerActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.PaymentActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.PortraitCaptureActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.SignUpActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.StaffManagementActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.SuccessActivity;
import sa.edu.kau.fcit.cpit252.carwash.adapters.CustomerAdapter;
import sa.edu.kau.fcit.cpit252.carwash.adapters.StaffAdapter;
import sa.edu.kau.fcit.cpit252.carwash.adapters.StaffPerformanceAdapter;
import sa.edu.kau.fcit.cpit252.carwash.bridge.VehiclePricing;
import sa.edu.kau.fcit.cpit252.carwash.bridge.WashPackage;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.database.OrderManager;
import sa.edu.kau.fcit.cpit252.carwash.factory.UserFactory;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.Owner;
import sa.edu.kau.fcit.cpit252.carwash.models.User;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventBus;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventListener;

import static org.junit.Assert.*;

public class ProjectRelationshipTest {

    @Test
    public void userModelClasses_shouldHaveCorrectInheritanceRelations() throws Exception {
        assertTrue(Modifier.isAbstract(User.class.getModifiers()));
        assertTrue(User.class.isAssignableFrom(Customer.class));
        assertTrue(User.class.isAssignableFrom(Cashier.class));
        assertTrue(User.class.isAssignableFrom(Owner.class));

        assertNotNull(Customer.class.getConstructor());
        assertNotNull(Cashier.class.getConstructor());
        assertNotNull(Owner.class.getConstructor());

        assertNotNull(Customer.class.getConstructor(String.class, String.class, String.class, String.class));
        assertNotNull(Cashier.class.getConstructor(String.class, String.class, String.class, String.class));
        assertNotNull(Owner.class.getConstructor(String.class, String.class, String.class, String.class));
    }

    @Test
    public void factoryClass_shouldExposeStaticCreateUserMethodReturningUser() throws Exception {
        Method createUser = UserFactory.class.getDeclaredMethod(
                "createUser",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class
        );

        assertTrue(Modifier.isStatic(createUser.getModifiers()));
        assertEquals(User.class, createUser.getReturnType());
    }

    @Test
    public void bridgePattern_shouldConnectPackageAbstractionToVehicleImplementation() throws Exception {
        assertTrue(Modifier.isAbstract(WashPackage.class.getModifiers()));
        assertTrue(VehiclePricing.class.isInterface());
        assertEquals(VehiclePricing.class, WashPackage.class.getDeclaredField("vehiclePricing").getType());
    }

    @Test
    public void observerPattern_shouldExposeListenerAndEventBusMethods() throws Exception {
        assertTrue(WashEventListener.class.isInterface());

        assertEquals(void.class, WashEventListener.class
                .getDeclaredMethod("onWashDeducted", String.class, String.class, String.class)
                .getReturnType());
        assertEquals(void.class, WashEventListener.class
                .getDeclaredMethod("onOrderCompleted", String.class)
                .getReturnType());

        assertNotNull(WashEventBus.class.getDeclaredMethod("getInstance"));
        assertNotNull(WashEventBus.class.getDeclaredMethod("subscribe", WashEventListener.class));
        assertNotNull(WashEventBus.class.getDeclaredMethod("unsubscribe", WashEventListener.class));
        assertNotNull(WashEventBus.class.getDeclaredMethod("publishWashDeducted", String.class, String.class, String.class));
        assertNotNull(WashEventBus.class.getDeclaredMethod("publishOrderCompleted", String.class));
    }

    @Test
    public void databaseAndOrderManagers_shouldExposeExpectedPublicMethodsWithoutCallingFirebase() throws Exception {
        Constructor<DatabaseManager> constructor = DatabaseManager.class.getDeclaredConstructor();
        assertTrue("DatabaseManager should control object creation using a private constructor", Modifier.isPrivate(constructor.getModifiers()));

        Method getInstance = DatabaseManager.class.getDeclaredMethod("getInstance");
        assertTrue(Modifier.isStatic(getInstance.getModifiers()));
        assertEquals(DatabaseManager.class, getInstance.getReturnType());

        assertNotNull(DatabaseManager.class.getDeclaredMethod("getAuth"));
        assertNotNull(DatabaseManager.class.getDeclaredMethod("getDb"));

        assertNotNull(OrderManager.class.getConstructor());
        assertNotNull(OrderManager.class.getDeclaredMethod(
                "saveNewOrder",
                String.class,
                String.class,
                String.class,
                String.class,
                com.google.android.gms.tasks.OnCompleteListener.class
        ));
        assertNotNull(OrderManager.class.getDeclaredMethod("deductWash", String.class, OrderManager.OperationCallback.class));
        assertNotNull(OrderManager.class.getDeclaredMethod("saveWashReport", String.class, String.class, String.class, String.class, OrderManager.OperationCallback.class));
    }

    @Test
    public void activities_shouldExtendAppCompatActivity() {
        Class<?>[] activities = new Class<?>[] {
                MainActivity.class,
                LoginActivity.class,
                SignUpActivity.class,
                CustomerActivity.class,
                OrdersActivity.class,
                PaymentActivity.class,
                SuccessActivity.class,
                OwnerActivity.class,
                AddCashierActivity.class,
                StaffManagementActivity.class,
                CustomerBlacklistActivity.class,
                CashierActivity.class,
                AnalyticsActivity.class,
                PortraitCaptureActivity.class
        };

        for (Class<?> activity : activities) {
            assertTrue(activity.getSimpleName() + " should extend AppCompatActivity",
                    AppCompatActivity.class.isAssignableFrom(activity));
        }
    }

    @Test
    public void adapters_shouldExposeCorrectUiAdapterRelationsAndCallbacks() {
        assertTrue(BaseAdapter.class.isAssignableFrom(CustomerAdapter.class));
        assertTrue(BaseAdapter.class.isAssignableFrom(StaffAdapter.class));
        assertTrue(RecyclerView.Adapter.class.isAssignableFrom(StaffPerformanceAdapter.class));

        assertTrue(CustomerAdapter.OnBlockToggleListener.class.isInterface());
        assertTrue(StaffAdapter.OnDeleteListener.class.isInterface());
    }
}
