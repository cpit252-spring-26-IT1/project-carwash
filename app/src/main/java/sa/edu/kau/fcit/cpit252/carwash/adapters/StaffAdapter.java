package sa.edu.kau.fcit.cpit252.carwash.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.models.Cashier;
public class StaffAdapter extends BaseAdapter {
    public interface OnDeleteListener {
        void onDelete(String userId, Cashier cashier);
    }

    private final Context context;
    private final List<String> userIds;
    private final List<Cashier> cashiers;
    private final OnDeleteListener deleteListener;

    public StaffAdapter(Context context, List<String> userIds, List<Cashier> cashiers,
                        OnDeleteListener deleteListener) {
        this.context = context;
        this.userIds = userIds;
        this.cashiers = cashiers;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getCount() {
        return cashiers.size();
    }

    @Override
    public Object getItem(int position) {
        return cashiers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cashier, parent, false);
        }

        final Cashier cashier = cashiers.get(position);
        final String userId = userIds.get(position);

        TextView tvName = convertView.findViewById(R.id.tvCashierName);
        TextView tvEmail = convertView.findViewById(R.id.tvCashierEmail);
        TextView tvRole = convertView.findViewById(R.id.tvCashierRole);
        Button btnDelete = convertView.findViewById(R.id.btnDeleteCashier);

        tvName.setText(cashier.getFullName());
        tvEmail.setText("Email: " + (cashier.getEmail() == null ? "--" : cashier.getEmail()));
        tvRole.setText("Role: " + cashier.getRole());

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteListener != null) {
                    deleteListener.onDelete(userId, cashier);
                }
            }
        });

        return convertView;
    }


}
