package sa.edu.kau.fcit.cpit252.carwash.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sa.edu.kau.fcit.cpit252.carwash.activities.AnalyticsActivity;
import sa.edu.kau.fcit.cpit252.carwash.adapters.StaffPerformanceAdapter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 36)
public class AnalyticsRenderingTest {

    private AnalyticsActivity activity;
    private Context context;

    @Before
    public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        activity = new AnalyticsActivity();

        set("tvTodayPeak", new TextView(context));
        set("tvTodayPackage", new TextView(context));
        set("cardPeriodInsights", new View(context));
        set("tvDay1", new TextView(context));
        set("tvDay2", new TextView(context));
        set("tvDay3", new TextView(context));
        set("tvPeriodTopPackage", new TextView(context));
        set("barChart", new BarChart(context));
        set("pieChart", new PieChart(context));
        set("layoutStaff", new LinearLayout(context));

        @SuppressWarnings("unchecked")
        List<StaffPerformanceAdapter.Row> staffRows = (List<StaffPerformanceAdapter.Row>) get("staffRows");
        set("staffAdapter", new StaffPerformanceAdapter(staffRows));
    }

    @Test
    public void renderTodayHighlights_shouldShowNoDataAndTopPackageStates() throws Exception {
        int[] emptyHours = new int[24];
        Map<String, Integer> emptyPackages = new HashMap<>();

        call("renderTodayHighlights", new Class[]{int[].class, Map.class}, emptyHours, emptyPackages);
        assertEquals("• Busiest Hour: no washes yet today", text("tvTodayPeak"));
        assertEquals("• Top Package: --", text("tvTodayPackage"));

        int[] hours = new int[24];
        hours[14] = 5;
        Map<String, Integer> packages = new HashMap<>();
        packages.put("Full Service", 3);
        packages.put("Exterior Only", 1);

        call("renderTodayHighlights", new Class[]{int[].class, Map.class}, hours, packages);
        assertTrue(text("tvTodayPeak").contains("2:00 PM"));
        assertEquals("• Top Package: Full Service", text("tvTodayPackage"));
    }

    @Test
    public void renderPeriodInsights_shouldHideForTodayAndRankWeekDaysForLongerPeriods() throws Exception {
        int[] weekdayCounts = new int[]{1, 5, 2, 9, 0, 3, 4};
        Map<String, Integer> packages = new HashMap<>();
        packages.put("Interior Only", 4);
        packages.put("Full Service", 10);

        call("renderPeriodInsights", new Class[]{int.class, int[].class, Map.class}, 0, weekdayCounts, packages);
        assertEquals(View.GONE, ((View) get("cardPeriodInsights")).getVisibility());

        call("renderPeriodInsights", new Class[]{int.class, int[].class, Map.class}, 1, weekdayCounts, packages);
        assertEquals(View.VISIBLE, ((View) get("cardPeriodInsights")).getVisibility());
        assertTrue(text("tvDay1").contains("Wed"));
        assertTrue(text("tvDay2").contains("Mon"));
        assertTrue(text("tvDay3").contains("Sat"));
        assertTrue(text("tvPeriodTopPackage").contains("Full Service"));
    }

    @Test
    public void renderCharts_shouldPopulateBarAndPieChartData() throws Exception {
        int[] hourCounts = new int[24];
        hourCounts[8] = 2;
        hourCounts[9] = 4;
        int[] weekdayCounts = new int[]{1, 2, 3, 4, 5, 6, 7};

        call("renderBarChart", new Class[]{int.class, int[].class, int[].class}, 0, hourCounts, weekdayCounts);
        BarChart bar = (BarChart) get("barChart");
        assertNotNull(bar.getData());
        assertEquals(24, bar.getData().getDataSetByIndex(0).getEntryCount());

        call("renderBarChart", new Class[]{int.class, int[].class, int[].class}, 2, hourCounts, weekdayCounts);
        assertEquals(7, bar.getData().getDataSetByIndex(0).getEntryCount());

        Map<String, Integer> packages = new HashMap<>();
        packages.put("Full Service", 3);
        packages.put("Exterior Only", 2);
        call("renderPieChart", new Class[]{Map.class}, packages);
        PieChart pie = (PieChart) get("pieChart");
        assertNotNull(pie.getData());
        assertEquals(2, pie.getData().getDataSetByIndex(0).getEntryCount());
    }

    @Test
    public void renderStaffList_shouldSortCashiersAndHideWhenEmpty() throws Exception {
        Map<String, Integer> cashierCounts = new HashMap<>();
        cashierCounts.put("Sara", 4);
        cashierCounts.put("Omar", 8);

        call("renderStaffList", new Class[]{Map.class}, cashierCounts);
        @SuppressWarnings("unchecked")
        List<StaffPerformanceAdapter.Row> rows = (List<StaffPerformanceAdapter.Row>) get("staffRows");
        assertEquals(2, rows.size());
        assertEquals("Omar", rows.get(0).cashierName);
        assertEquals(8, rows.get(0).washCount);
        assertEquals(View.VISIBLE, ((View) get("layoutStaff")).getVisibility());

        call("renderStaffList", new Class[]{Map.class}, new HashMap<String, Integer>());
        assertEquals(0, rows.size());
        assertEquals(View.GONE, ((View) get("layoutStaff")).getVisibility());
    }

    @Test
    public void helperMethods_shouldFormatHoursAndComputeDateRanges() throws Exception {
        assertEquals("12:00 AM", call("formatHour", new Class[]{int.class}, 0));
        assertEquals("12:00 PM", call("formatHour", new Class[]{int.class}, 12));
        assertEquals("11:00 PM", call("formatHour", new Class[]{int.class}, 23));
        assertNotNull(call("computeStartDate", new Class[]{int.class}, 0));
        assertNotNull(call("computeStartDate", new Class[]{int.class}, 1));
        assertNotNull(call("computeStartDate", new Class[]{int.class}, 2));
    }

    private String text(String fieldName) throws Exception {
        return ((TextView) get(fieldName)).getText().toString();
    }

    private Object call(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = AnalyticsActivity.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(activity, args);
    }

    private Object get(String fieldName) throws Exception {
        Field field = AnalyticsActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(activity);
    }

    private void set(String fieldName, Object value) throws Exception {
        Field field = AnalyticsActivity.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(activity, value);
    }
}
