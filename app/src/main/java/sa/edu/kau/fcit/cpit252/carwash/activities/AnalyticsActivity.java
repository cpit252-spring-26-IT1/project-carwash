package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import sa.edu.kau.fcit.cpit252.carwash.R;
import sa.edu.kau.fcit.cpit252.carwash.adapters.StaffPerformanceAdapter;
import sa.edu.kau.fcit.cpit252.carwash.database.DatabaseManager;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventBus;
import sa.edu.kau.fcit.cpit252.carwash.observer.WashEventListener;

public class AnalyticsActivity extends AppCompatActivity implements WashEventListener {

    private static final int PERIOD_TODAY = 0;
    private static final int PERIOD_WEEK = 1;
    private static final int PERIOD_MONTH = 2;

    private Spinner spinnerPeriod;
    private TextView tvTodayPeak, tvTodayPackage;
    private View cardPeriodInsights;
    private TextView tvDay1, tvDay2, tvDay3, tvPeriodTopPackage;
    private BarChart barChart;
    private PieChart pieChart;
    private RecyclerView rvStaff;
    private LinearLayout layoutStaff;

    private final List<StaffPerformanceAdapter.Row> staffRows = new ArrayList<>();
    private StaffPerformanceAdapter staffAdapter;

    private int currentPeriod = PERIOD_TODAY;

    private ListenerRegistration reportsListener;
    private boolean isInitialReportsSync = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        TextView tvBack = findViewById(R.id.tvBackAnalytics);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        tvTodayPeak = findViewById(R.id.tvTodayPeak);
        tvTodayPackage = findViewById(R.id.tvTodayPackage);
        cardPeriodInsights = findViewById(R.id.cardPeriodInsights);
        tvDay1 = findViewById(R.id.tvDay1);
        tvDay2 = findViewById(R.id.tvDay2);
        tvDay3 = findViewById(R.id.tvDay3);
        tvPeriodTopPackage = findViewById(R.id.tvPeriodTopPackage);
        barChart = findViewById(R.id.barChartTimeline);
        pieChart = findViewById(R.id.pieChartPackages);
        rvStaff = findViewById(R.id.rvStaffPerformance);
        layoutStaff = findViewById(R.id.layoutStaff);

        staffAdapter = new StaffPerformanceAdapter(staffRows);
        rvStaff.setLayoutManager(new LinearLayoutManager(this));
        rvStaff.setAdapter(staffAdapter);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadAnalytics(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        loadAnalytics(PERIOD_TODAY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WashEventBus.getInstance().subscribe(this);

        isInitialReportsSync = true;
        reportsListener = DatabaseManager.getInstance().getDb()
                .collection("WashReports")
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;
                    if (isInitialReportsSync) {
                        isInitialReportsSync = false;
                        return;
                    }

                    for (DocumentChange change : snap.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String pkg = change.getDocument().getString("packageName");
                            WashEventBus.getInstance().publishWashDeducted(null, null, pkg);
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        WashEventBus.getInstance().unsubscribe(this);
        if (reportsListener != null) {
            reportsListener.remove();
            reportsListener = null;
        }
    }

    @Override
    public void onWashDeducted(String orderId, String customerName, String packageName) {
        loadAnalytics(currentPeriod);
        Toast.makeText(this, "Dashboard updated — new wash recorded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOrderCompleted(String orderId) {
        loadAnalytics(currentPeriod);
    }

    private void loadAnalytics(int period) {
        final Date startDate = computeStartDate(period);

        DatabaseManager.getInstance().getDb()
                .collection("WashReports")
                .get()
                .addOnSuccessListener(snap -> processData(snap, period, startDate))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private Date computeStartDate(int period) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (period == PERIOD_WEEK) {
            cal.add(Calendar.DAY_OF_YEAR, -6);
        } else if (period == PERIOD_MONTH) {
            cal.add(Calendar.DAY_OF_YEAR, -29);
        }
        return cal.getTime();
    }

    private void processData(QuerySnapshot snap, int period, Date startDate) {
        Map<String, Integer> packageCounts = new HashMap<>();
        Map<String, Integer> cashierCounts = new HashMap<>();
        int[] hourCounts = new int[24];
        int[] weekdayCounts = new int[7];

        Map<String, Integer> todayPackageCounts = new HashMap<>();
        int[] todayHourCounts = new int[24];

        Date startOfToday = computeStartDate(PERIOD_TODAY);

        for (QueryDocumentSnapshot doc : snap) {
            Timestamp ts = doc.getTimestamp("timestamp");
            if (ts == null) continue;
            Date when = ts.toDate();
            String pkg = doc.getString("packageName");
            String cashier = doc.getString("cashierName");

            if (!when.before(startOfToday)) {
                if (pkg != null) {
                    todayPackageCounts.merge(pkg, 1, Integer::sum);
                }
                Calendar c = Calendar.getInstance();
                c.setTime(when);
                int hr = c.get(Calendar.HOUR_OF_DAY);
                todayHourCounts[hr]++;
            }

            if (when.before(startDate)) continue;

            if (pkg != null) {
                packageCounts.merge(pkg, 1, Integer::sum);
            }
            if (cashier != null) {
                cashierCounts.merge(cashier, 1, Integer::sum);
            }

            Calendar c = Calendar.getInstance();
            c.setTime(when);
            int hr = c.get(Calendar.HOUR_OF_DAY);
            hourCounts[hr]++;
            int dow = c.get(Calendar.DAY_OF_WEEK) - 1;
            weekdayCounts[dow]++;
        }

        renderTodayHighlights(todayHourCounts, todayPackageCounts);
        renderPeriodInsights(period, weekdayCounts, packageCounts);
        renderBarChart(period, hourCounts, weekdayCounts);
        renderPieChart(packageCounts);
        renderStaffList(cashierCounts);
    }

    private void renderTodayHighlights(int[] hourCounts, Map<String, Integer> pkgCounts) {
        int peakHour = -1, peakCount = 0;
        for (int i = 0; i < 24; i++) {
            if (hourCounts[i] > peakCount) {
                peakCount = hourCounts[i];
                peakHour = i;
            }
        }
        if (peakHour < 0) {
            tvTodayPeak.setText("• Busiest Hour: no washes yet today");
        } else {
            tvTodayPeak.setText(String.format(Locale.getDefault(),
                    "• Busiest Hour: %s (%d washes)", formatHour(peakHour), peakCount));
        }

        String topPkg = null;
        int topCount = 0;
        for (Map.Entry<String, Integer> e : pkgCounts.entrySet()) {
            if (e.getValue() > topCount) {
                topCount = e.getValue();
                topPkg = e.getKey();
            }
        }
        tvTodayPackage.setText(topPkg == null
                ? "• Top Package: --"
                : "• Top Package: " + topPkg);
    }

    private void renderPeriodInsights(int period, int[] weekdayCounts, Map<String, Integer> pkgCounts) {
        if (period == PERIOD_TODAY) {
            cardPeriodInsights.setVisibility(View.GONE);
            return;
        }
        cardPeriodInsights.setVisibility(View.VISIBLE);

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        Integer[] order = {0, 1, 2, 3, 4, 5, 6};
        java.util.Arrays.sort(order, (a, b) -> Integer.compare(weekdayCounts[b], weekdayCounts[a]));

        tvDay1.setText(formatDayRank("🥇", dayNames, weekdayCounts, order[0]));
        tvDay2.setText(formatDayRank("🥈", dayNames, weekdayCounts, order[1]));
        tvDay3.setText(formatDayRank("🥉", dayNames, weekdayCounts, order[2]));

        String topPkg = null;
        int topCount = 0;
        for (Map.Entry<String, Integer> e : pkgCounts.entrySet()) {
            if (e.getValue() > topCount) {
                topCount = e.getValue();
                topPkg = e.getKey();
            }
        }
        tvPeriodTopPackage.setText(topPkg == null
                ? "🔥 Top Package: no data"
                : String.format(Locale.getDefault(),
                "🔥 Top Package: %s (%d times)", topPkg, topCount));
    }

    private String formatDayRank(String medal, String[] dayNames, int[] counts, int idx) {
        return String.format(Locale.getDefault(),
                "%s %s: %d washes", medal, dayNames[idx], counts[idx]);
    }

    private void renderBarChart(int period, int[] hourCounts, int[] weekdayCounts) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String label;

        if (period == PERIOD_TODAY) {
            label = "Washes per Hour";
            for (int i = 0; i < 24; i++) {
                entries.add(new BarEntry(i, hourCounts[i]));
                labels.add(String.format(Locale.getDefault(), "%02d", i));
            }
        } else {
            label = "Washes per Weekday";
            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, weekdayCounts[i]));
                labels.add(dayNames[i]);
            }
        }

        BarDataSet set = new BarDataSet(entries, label);
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(10f);

        BarData data = new BarData(set);
        data.setBarWidth(0.85f);

        barChart.fitScreen();
        barChart.zoom(1f, 1f, 0f, 0f);

        barChart.setData(data);

        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setTouchEnabled(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.size(), false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.setFitBars(true);

        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);

        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private void renderPieChart(Map<String, Integer> pkgCounts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> e : pkgCounts.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "Packages");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        set.setValueTextSize(11f);
        set.setSliceSpace(2f);

        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(10f);
        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);
        pieChart.invalidate();
    }

    private void renderStaffList(Map<String, Integer> cashierCounts) {
        staffRows.clear();
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(cashierCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (Map.Entry<String, Integer> e : sorted) {
            staffRows.add(new StaffPerformanceAdapter.Row(e.getKey(), e.getValue()));
        }
        staffAdapter.notifyDataSetChanged();
        layoutStaff.setVisibility(staffRows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String formatHour(int hour24) {
        int h = hour24 % 12;
        if (h == 0) h = 12;
        String suffix = hour24 < 12 ? "AM" : "PM";
        return String.format(Locale.getDefault(), "%d:00 %s", h, suffix);
    }
}