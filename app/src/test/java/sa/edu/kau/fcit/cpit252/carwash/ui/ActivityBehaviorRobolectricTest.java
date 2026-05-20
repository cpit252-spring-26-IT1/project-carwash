package sa.edu.kau.fcit.cpit252.carwash.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Method;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.activities.AddCashierActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CashierActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CustomerActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.CustomerBlacklistActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.LoginActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.MainActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.OrdersActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.OwnerActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.PaymentActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.SignUpActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.StaffManagementActivity;
import sa.edu.kau.fcit.cpit252.carwash.activities.SuccessActivity;
import sa.edu.kau.fcit.cpit252.carwash.database.DataStore;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;
import sa.edu.kau.fcit.cpit252.carwash.models.Owner;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class ActivityBehaviorRobolectricTest {

    @Test
    public void mainScreenButtons_shouldOpenLoginAndSignupScreens() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).create().get();

        activity.findViewById(R.id.btnLogin).performClick();
        Intent loginIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(), loginIntent.getComponent().getClassName());

        activity.findViewById(R.id.btnSignUp).performClick();
        Intent signupIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(SignUpActivity.class.getName(), signupIntent.getComponent().getClassName());
    }

    @Test
    public void loginScreen_shouldValidateEmptyInputsAndNavigateByRole() throws Exception {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).create().get();

        activity.findViewById(R.id.btnLogin).performClick();
        assertEquals("Please enter email and password", ShadowToast.getTextOfLatestToast());

        assertNavigationForUser(new Owner("Omar", "Ali", "owner@example.com", "123456"), OwnerActivity.class);
        assertNavigationForUser(new Cashier("Sara", "Ali", "cashier@example.com", "123456"), CashierActivity.class);
        assertNavigationForUser(new Customer("Nora", "Ali", "customer@example.com", "123456"), CustomerActivity.class);
    }

    @Test
    public void signUpScreen_shouldValidateRequiredNameAndEmailFields() throws Exception {
        SignUpActivity activity = Robolectric.buildActivity(SignUpActivity.class).create().get();

        activity.findViewById(R.id.btnSignup).performClick();
        assertEquals("Please Fill All Fields", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etFirstName)).setText("Ali1");
        ((EditText) activity.findViewById(R.id.etLastName)).setText("Saleh");
        ((EditText) activity.findViewById(R.id.etEmail)).setText("ali@example.com");
        ((EditText) activity.findViewById(R.id.etPassword)).setText("123456");
        activity.findViewById(R.id.btnSignup).performClick();
        assertEquals("Names can only contain letters", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etFirstName)).setText("Ali");
        ((EditText) activity.findViewById(R.id.etEmail)).setText("invalid-email");
        activity.findViewById(R.id.btnSignup).performClick();
        assertEquals("Please enter a valid email address", ShadowToast.getTextOfLatestToast());

        Method isValidName = SignUpActivity.class.getDeclaredMethod("isValidName", String.class);
        Method isValidEmail = SignUpActivity.class.getDeclaredMethod("isValidEmail", String.class);
        isValidName.setAccessible(true);
        isValidEmail.setAccessible(true);
        assertEquals(true, isValidName.invoke(activity, "Hashim"));
        assertEquals(false, isValidName.invoke(activity, "Hashim1"));
        assertEquals(true, isValidEmail.invoke(activity, "student@example.com"));
        assertEquals(false, isValidEmail.invoke(activity, "bad-email"));
    }

    @Test
    public void customerScreen_shouldDisplayPricesSelectPackagesAndOpenOrders() throws Exception {
        CustomerActivity activity = Robolectric.buildActivity(CustomerActivity.class).setup().get();

        Method updatePrices = CustomerActivity.class.getDeclaredMethod("updatePrices", int.class);
        updatePrices.setAccessible(true);
        updatePrices.invoke(activity, 0);
        assertEquals("SAR 200.0", ((TextView) activity.findViewById(R.id.tvPriceFull)).getText().toString());
        assertEquals("SAR 100.0", ((TextView) activity.findViewById(R.id.tvPriceOutside)).getText().toString());
        assertEquals("SAR 80.0", ((TextView) activity.findViewById(R.id.tvPriceInside)).getText().toString());

        updatePrices.invoke(activity, 1);
        assertEquals("SAR 300.0", ((TextView) activity.findViewById(R.id.tvPriceFull)).getText().toString());
        assertEquals("SAR 150.0", ((TextView) activity.findViewById(R.id.tvPriceOutside)).getText().toString());
        assertEquals("SAR 120.0", ((TextView) activity.findViewById(R.id.tvPriceInside)).getText().toString());

        ((CardView) activity.findViewById(R.id.cardFull)).performClick();
        ((CardView) activity.findViewById(R.id.cardOutside)).performClick();
        ((CardView) activity.findViewById(R.id.cardInside)).performClick();

        activity.findViewById(R.id.btnMyOrders).performClick();
        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertEquals(OrdersActivity.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void paymentScreen_shouldRejectInvalidPaymentInputsBeforeSavingOrder() {
        Intent intent = new Intent();
        intent.putExtra("package_name", "Full Service");
        intent.putExtra("price", "SAR 200.0");
        intent.putExtra("vehicle", "Sedan");
        PaymentActivity activity = Robolectric.buildActivity(PaymentActivity.class, intent).create().get();

        activity.findViewById(R.id.btnConfirmPur).performClick();
        assertEquals("Please fill in all payment details", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etCardNumber)).setText("4111111111111111");
        ((EditText) activity.findViewById(R.id.etExpiry)).setText("0226");
        ((EditText) activity.findViewById(R.id.etCVV)).setText("123");
        activity.findViewById(R.id.btnConfirmPur).performClick();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Expiry must be in MM/YY format"));

        ((EditText) activity.findViewById(R.id.etExpiry)).setText("AA/26");
        activity.findViewById(R.id.btnConfirmPur).performClick();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Expiry must be numbers"));

        ((EditText) activity.findViewById(R.id.etExpiry)).setText("13/26");
        activity.findViewById(R.id.btnConfirmPur).performClick();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Invalid month"));
    }

    @Test
    public void addCashierScreen_shouldValidateFieldsBeforeCallingFirebase() {
        DataStore.setCurrentUser(null);
        AddCashierActivity activity = Robolectric.buildActivity(AddCashierActivity.class).create().get();

        activity.findViewById(R.id.btnSaveCashier).performClick();
        assertEquals("Please fill all fields", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etCashierFirstName)).setText("Nora1");
        ((EditText) activity.findViewById(R.id.etCashierLastName)).setText("Saleh");
        ((EditText) activity.findViewById(R.id.etCashierEmail)).setText("cashier@example.com");
        ((EditText) activity.findViewById(R.id.etCashierPassword)).setText("123456");
        activity.findViewById(R.id.btnSaveCashier).performClick();
        assertEquals("Names can only contain letters", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etCashierFirstName)).setText("Nora");
        ((EditText) activity.findViewById(R.id.etCashierEmail)).setText("bad-email");
        activity.findViewById(R.id.btnSaveCashier).performClick();
        assertEquals("Invalid email address", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etCashierEmail)).setText("cashier@example.com");
        ((EditText) activity.findViewById(R.id.etCashierPassword)).setText("123");
        activity.findViewById(R.id.btnSaveCashier).performClick();
        assertEquals("Password must be at least 6 characters", ShadowToast.getTextOfLatestToast());

        ((EditText) activity.findViewById(R.id.etCashierPassword)).setText("123456");
        activity.findViewById(R.id.btnSaveCashier).performClick();
        assertEquals("Session expired. Please log in again.", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void cashierScreen_shouldShowLoggedInUserAndValidateSearchBeforeLookup() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("USER_NAME", "Sara");
        CashierActivity activity = Robolectric.buildActivity(CashierActivity.class, intent).create().get();

        assertEquals("Logged in as: Sara", ((TextView) activity.findViewById(R.id.tvLoggedInCashier)).getText().toString());

        activity.findViewById(R.id.btnSearch).performClick();
        assertEquals("Enter an Order ID or scan a QR code", ShadowToast.getTextOfLatestToast());

        activity.findViewById(R.id.btnConfirmWash).performClick();
        assertEquals("Look up a customer first", ShadowToast.getTextOfLatestToast());

        Method clear = CashierActivity.class.getDeclaredMethod("clearCustomerCard");
        clear.setAccessible(true);
        clear.invoke(activity);
        assertEquals("Customer: --", ((TextView) activity.findViewById(R.id.tvCustomerName)).getText().toString());
        assertEquals("Car Type: --", ((TextView) activity.findViewById(R.id.tvCarType)).getText().toString());
        assertEquals("Package: --", ((TextView) activity.findViewById(R.id.tvPackageType)).getText().toString());
        assertEquals("Remaining: 0 Washes", ((TextView) activity.findViewById(R.id.tvBalanceStatus)).getText().toString());
        assertEquals(false, ((Button) activity.findViewById(R.id.btnConfirmWash)).isEnabled());
    }

    @Test
    public void ownerAndManagementScreens_shouldOpenExpectedPagesWithoutCallingFirebase() {
        OwnerActivity owner = Robolectric.buildActivity(OwnerActivity.class).create().get();

        owner.findViewById(R.id.btnManageStaff).performClick();
        assertEquals(StaffManagementActivity.class.getName(), shadowOf(owner).getNextStartedActivity().getComponent().getClassName());

        owner.findViewById(R.id.btnManageBlacklist).performClick();
        assertEquals(CustomerBlacklistActivity.class.getName(), shadowOf(owner).getNextStartedActivity().getComponent().getClassName());

        StaffManagementActivity staff = Robolectric.buildActivity(StaffManagementActivity.class).create().get();
        assertNotNull(staff.findViewById(R.id.listStaff));
        staff.findViewById(R.id.btnAddCashier).performClick();
        assertEquals(AddCashierActivity.class.getName(), shadowOf(staff).getNextStartedActivity().getComponent().getClassName());

        CustomerBlacklistActivity blacklist = Robolectric.buildActivity(CustomerBlacklistActivity.class).create().get();
        assertNotNull(blacklist.findViewById(R.id.listCustomers));
        ((EditText) blacklist.findViewById(R.id.etSearchCustomer)).setText("Ali");
        assertNotNull(blacklist.findViewById(R.id.tvEmptyCustomers));
    }

    @Test
    public void successScreen_shouldNavigateToHomeOrOrders() {
        SuccessActivity activity = Robolectric.buildActivity(SuccessActivity.class).create().get();

        activity.findViewById(R.id.btnViewOrdersSuccess).performClick();
        Intent ordersIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(OrdersActivity.class.getName(), ordersIntent.getComponent().getClassName());

        activity.findViewById(R.id.btnBackHome).performClick();
        Intent homeIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(CustomerActivity.class.getName(), homeIntent.getComponent().getClassName());
    }

    private void assertNavigationForUser(Object user, Class<?> expectedActivity) throws Exception {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class).create().get();
        Method handleNavigation = LoginActivity.class.getDeclaredMethod("handleNavigation", sa.edu.kau.fcit.cpit252.carwash.models.User.class);
        handleNavigation.setAccessible(true);
        handleNavigation.invoke(activity, user);

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertNotNull(intent);
        assertEquals(expectedActivity.getName(), intent.getComponent().getClassName());
        assertEquals(((sa.edu.kau.fcit.cpit252.carwash.models.User) user).getFirstName(), intent.getStringExtra("USER_NAME"));
    }
}
