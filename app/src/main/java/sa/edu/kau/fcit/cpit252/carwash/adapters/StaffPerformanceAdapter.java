package sa.edu.kau.fcit.cpit252.carwash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sa.edu.kau.fcit.cpit252.carwash.R;

public class StaffPerformanceAdapter extends RecyclerView.Adapter<StaffPerformanceAdapter.VH> {

    public static class Row {
        public final String cashierName;
        public final int washCount;
        public Row(String cashierName, int washCount) {
            this.cashierName = cashierName;
            this.washCount = washCount;
        }
    }

    private final List<Row> rows;

    public StaffPerformanceAdapter(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_performance, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Row r = rows.get(position);
        holder.tvName.setText(r.cashierName == null ? "Unknown" : r.cashierName);
        holder.tvCount.setText(String.valueOf(r.washCount));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCount;
        VH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvCount = itemView.findViewById(R.id.tvStaffWashCount);
        }
    }
}