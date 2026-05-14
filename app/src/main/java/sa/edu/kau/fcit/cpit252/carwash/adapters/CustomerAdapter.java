package sa.edu.kau.fcit.cpit252.carwash.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.models.Customer;

public class CustomerAdapter extends BaseAdapter {

    public interface OnBlockToggleListener {
        void onToggle(String userId, Customer customer);
    }

    private final Context context;

    private final List<String> allUserIds;
    private final List<Customer> allCustomers;

    private final List<String> visibleUserIds = new ArrayList<>();
    private final List<Customer> visibleCustomers = new ArrayList<>();

    private final OnBlockToggleListener toggleListener;

    public CustomerAdapter(Context context, List<String> userIds, List<Customer> customers,
                           OnBlockToggleListener toggleListener) {
        this.context = context;
        this.allUserIds = userIds;
        this.allCustomers = customers;
        this.toggleListener = toggleListener;
        visibleUserIds.addAll(userIds);
        visibleCustomers.addAll(customers);
    }


    public void filter(String query) {
        visibleUserIds.clear();
        visibleCustomers.clear();

        if (query == null || query.trim().isEmpty()) {
            visibleUserIds.addAll(allUserIds);
            visibleCustomers.addAll(allCustomers);
        } else {
            String q = query.toLowerCase(Locale.getDefault()).trim();
            for (int i = 0; i < allCustomers.size(); i++) {
                Customer c = allCustomers.get(i);
                String name = c.getFullName() == null ? "" : c.getFullName().toLowerCase(Locale.getDefault());
                String email = c.getEmail() == null ? "" : c.getEmail().toLowerCase(Locale.getDefault());
                if (name.contains(q) || email.contains(q)) {
                    visibleUserIds.add(allUserIds.get(i));
                    visibleCustomers.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public void notifyDataSetChanged() {
        if (visibleCustomers.isEmpty() && !allCustomers.isEmpty()) {
            visibleUserIds.clear();
            visibleCustomers.clear();
            visibleUserIds.addAll(allUserIds);
            visibleCustomers.addAll(allCustomers);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return visibleCustomers.size();
    }

    @Override
    public Object getItem(int position) {
        return visibleCustomers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_customer_blacklist, parent, false);
        }

        final Customer customer = visibleCustomers.get(position);
        final String userId = visibleUserIds.get(position);

        TextView tvName = convertView.findViewById(R.id.tvCustomerName);
        TextView tvEmail = convertView.findViewById(R.id.tvCustomerEmail);
        TextView tvStatus = convertView.findViewById(R.id.tvCustomerStatus);
        Button btnBlock = convertView.findViewById(R.id.btnBlockCustomer);

        tvName.setText(customer.getFullName());
        tvEmail.setText("Email: " + (customer.getEmail() == null ? "--" : customer.getEmail()));

        if (customer.isBlacklisted()) {
            tvStatus.setText("Status: Blocked");
            tvStatus.setTextColor(0xFFE74C3C);
            btnBlock.setText("Unblock");
            btnBlock.setBackgroundTintList(ColorStateList.valueOf(0xFF27AE60));
        } else {
            tvStatus.setText("Status: Active");
            tvStatus.setTextColor(0xFF27AE60);
            btnBlock.setText("Block");
            btnBlock.setBackgroundTintList(ColorStateList.valueOf(0xFFE74C3C));
        }

        btnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleListener != null) {
                    toggleListener.onToggle(userId, customer);
                }
            }
        });

        return convertView;
    }
}