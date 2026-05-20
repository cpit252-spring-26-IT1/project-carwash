package sa.edu.kau.fcit.cpit252.carwash.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class AdapterBehaviorTest {

    @Test
    public void customerAdapter_shouldFilterRenderStatusAndNotifyToggleListener() {
        Context context = ApplicationProvider.getApplicationContext();
        List<String> ids = Arrays.asList("u1", "u2");
        Customer active = new Customer("Ali", "Saleh", "ali@example.com", "123456");
        Customer blocked = new Customer("Nora", "Omar", "nora@example.com", "123456");
        blocked.setBlacklisted(true);
        List<Customer> customers = Arrays.asList(active, blocked);
        final String[] clickedId = new String[1];
        final Customer[] clickedCustomer = new Customer[1];

        CustomerAdapter adapter = new CustomerAdapter(context, ids, customers, (userId, customer) -> {
            clickedId[0] = userId;
            clickedCustomer[0] = customer;
        });

        assertEquals(2, adapter.getCount());
        assertSame(active, adapter.getItem(0));
        assertEquals(0, adapter.getItemId(0));

        adapter.filter("nora");
        assertEquals(1, adapter.getCount());
        View blockedView = adapter.getView(0, null, null);
        assertEquals("Nora Omar", ((TextView) blockedView.findViewById(R.id.tvCustomerName)).getText().toString());
        assertEquals("Email: nora@example.com", ((TextView) blockedView.findViewById(R.id.tvCustomerEmail)).getText().toString());
        assertEquals("Status: Blocked", ((TextView) blockedView.findViewById(R.id.tvCustomerStatus)).getText().toString());
        assertEquals("Unblock", ((Button) blockedView.findViewById(R.id.btnBlockCustomer)).getText().toString());

        blockedView.findViewById(R.id.btnBlockCustomer).performClick();
        assertEquals("u2", clickedId[0]);
        assertSame(blocked, clickedCustomer[0]);

        adapter.filter(null);
        assertEquals(2, adapter.getCount());
        View activeView = adapter.getView(0, blockedView, null);
        assertEquals("Status: Active", ((TextView) activeView.findViewById(R.id.tvCustomerStatus)).getText().toString());
        assertEquals("Block", ((Button) activeView.findViewById(R.id.btnBlockCustomer)).getText().toString());
    }

    @Test
    public void customerAdapter_shouldRestoreListWhenFilterHasNoMatch() {
        Context context = ApplicationProvider.getApplicationContext();
        Customer customer = new Customer("Ali", "Saleh", null, "123456");
        CustomerAdapter adapter = new CustomerAdapter(
                context,
                Arrays.asList("u1"),
                Arrays.asList(customer),
                null
        );

        adapter.filter("does-not-exist");
        assertEquals("notifyDataSetChanged restores the original list used by this app", 1, adapter.getCount());
        View view = adapter.getView(0, null, null);
        assertEquals("Email: --", ((TextView) view.findViewById(R.id.tvCustomerEmail)).getText().toString());
    }

    @Test
    public void staffAdapter_shouldRenderCashierAndNotifyDeleteListener() {
        Context context = ApplicationProvider.getApplicationContext();
        Cashier cashier = new Cashier("Sara", "Saleh", "sara@example.com", "123456");
        final String[] deletedId = new String[1];
        final Cashier[] deletedCashier = new Cashier[1];

        StaffAdapter adapter = new StaffAdapter(
                context,
                Arrays.asList("c1"),
                Arrays.asList(cashier),
                (userId, selectedCashier) -> {
                    deletedId[0] = userId;
                    deletedCashier[0] = selectedCashier;
                }
        );

        assertEquals(1, adapter.getCount());
        assertSame(cashier, adapter.getItem(0));
        assertEquals(0, adapter.getItemId(0));

        View view = adapter.getView(0, null, null);
        assertEquals("Sara Saleh", ((TextView) view.findViewById(R.id.tvCashierName)).getText().toString());
        assertEquals("Email: sara@example.com", ((TextView) view.findViewById(R.id.tvCashierEmail)).getText().toString());
        assertEquals("Role: CASHIER", ((TextView) view.findViewById(R.id.tvCashierRole)).getText().toString());

        view.findViewById(R.id.btnDeleteCashier).performClick();
        assertEquals("c1", deletedId[0]);
        assertSame(cashier, deletedCashier[0]);
    }

    @Test
    public void staffPerformanceAdapter_shouldBindRowsIncludingUnknownCashierName() {
        Context context = ApplicationProvider.getApplicationContext();
        List<StaffPerformanceAdapter.Row> rows = new ArrayList<>();
        rows.add(new StaffPerformanceAdapter.Row("Sara", 7));
        rows.add(new StaffPerformanceAdapter.Row(null, 2));

        StaffPerformanceAdapter adapter = new StaffPerformanceAdapter(rows);
        assertEquals(2, adapter.getItemCount());

        RecyclerView parent = new RecyclerView(context);
        parent.setLayoutManager(new LinearLayoutManager(context));
        RecyclerView.ViewHolder firstHolder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder((StaffPerformanceAdapter.VH) firstHolder, 0);
        assertEquals("Sara", ((TextView) firstHolder.itemView.findViewById(R.id.tvStaffName)).getText().toString());
        assertEquals("7", ((TextView) firstHolder.itemView.findViewById(R.id.tvStaffWashCount)).getText().toString());

        RecyclerView.ViewHolder secondHolder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder((StaffPerformanceAdapter.VH) secondHolder, 1);
        assertEquals("Unknown", ((TextView) secondHolder.itemView.findViewById(R.id.tvStaffName)).getText().toString());
        assertEquals("2", ((TextView) secondHolder.itemView.findViewById(R.id.tvStaffWashCount)).getText().toString());
    }
}
