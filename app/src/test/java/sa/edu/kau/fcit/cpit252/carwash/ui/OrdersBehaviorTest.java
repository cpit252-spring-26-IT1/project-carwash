package sa.edu.kau.fcit.cpit252.carwash.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sa.edu.kau.fcit.cpit252.carwash.activities.OrdersActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class OrdersBehaviorTest {

    private OrdersActivity activity;

    @Before
    public void setup() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        activity = new OrdersActivity();
        set("btnBackToHome", new Button(context));
        set("cvOrderCard", new CardView(context));
        set("tvNoOrdersMessage", new TextView(context));
        set("tvOrderName", new TextView(context));
        set("tvOrderPrice", new TextView(context));
        set("tvCarInfo", new TextView(context));
        set("tvWashCount", new TextView(context));
        set("tvDaysLeft", new TextView(context));
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setMax(5);
        set("pbWashes", progressBar);
        set("ivOrderQr", new ImageView(context));
        set("tvOrderId", new TextView(context));
    }

    @Test
    public void showActiveOrder_shouldRenderOrderDataProgressAndQrCode() throws Exception {
        DocumentSnapshot order = mock(DocumentSnapshot.class);
        when(order.getId()).thenReturn("order123456");
        when(order.getString("packageName")).thenReturn("Full Service");
        when(order.getString("packagePrice")).thenReturn("SAR 200");
        when(order.getString("vehicle")).thenReturn("Sedan");
        when(order.getString("washesUsed")).thenReturn("3");
        when(order.getString("maxWashes")).thenReturn("5");
        when(order.getString("shortCode")).thenReturn("ABC123");
        when(order.getTimestamp("createdAt")).thenReturn(null);

        call("showActiveOrder", new Class[]{DocumentSnapshot.class}, order);

        assertEquals(View.GONE, ((View) get("tvNoOrdersMessage")).getVisibility());
        assertEquals(View.VISIBLE, ((View) get("cvOrderCard")).getVisibility());
        assertEquals("Full Service", text("tvOrderName"));
        assertEquals("SAR 200", text("tvOrderPrice"));
        assertEquals("Vehicle: Sedan", text("tvCarInfo"));
        assertEquals("Washes used: 3 of 5", text("tvWashCount"));
        assertEquals("Order ID: ABC123", text("tvOrderId"));
        assertEquals("Expires in 30 days", text("tvDaysLeft"));
        assertEquals(3, ((ProgressBar) get("pbWashes")).getProgress());
        assertNotNull(((ImageView) get("ivOrderQr")).getDrawable());
    }

    @Test
    public void refreshDaysLeft_shouldUpdateStoredDaysAndExpireOldActiveOrders() throws Exception {
        DocumentSnapshot order = mock(DocumentSnapshot.class);
        DocumentReference ref = mock(DocumentReference.class);
        Date createdLongAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40));

        when(order.getTimestamp("createdAt")).thenReturn(new Timestamp(createdLongAgo));
        when(order.getString("daysLeft")).thenReturn("5");
        when(order.getString("status")).thenReturn("active");
        when(order.getReference()).thenReturn(ref);

        call("refreshDaysLeft", new Class[]{DocumentSnapshot.class}, order);

        assertEquals("Expires in 0 days", text("tvDaysLeft"));
        verify(ref).update("daysLeft", "0");
        verify(ref).update("status", "expired");
    }

    @Test
    public void generateQrCode_shouldPlaceBitmapInImageView() throws Exception {
        call("generateQrCode", new Class[]{String.class}, "XYZ789");
        assertNotNull(((ImageView) get("ivOrderQr")).getDrawable());
    }

    @Test
    public void orderEventMethods_shouldIgnoreUnrelatedOrderIdsWithoutChangingState() throws Exception {
        set("currentOrderId", "ORDER-1");
        activity.onWashDeducted("OTHER", "Ali", "Full Service");
        activity.onOrderCompleted("OTHER");
        assertEquals("ORDER-1", get("currentOrderId"));
    }

    private String text(String fieldName) throws Exception {
        return ((TextView) get(fieldName)).getText().toString();
    }

    private Object call(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = OrdersActivity.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(activity, args);
    }

    private Object get(String fieldName) throws Exception {
        Field field = OrdersActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(activity);
    }

    private void set(String fieldName, Object value) throws Exception {
        Field field = OrdersActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }
}
