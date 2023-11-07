package com.example.stepappv4.ui.Day;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.R;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.databinding.FragmentDayBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DayFragment extends Fragment {

    AnyChartView anyChartView;
    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public Map<String, Integer> stepsByDay = null;
    private FragmentDayBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create column chart
        anyChartView = root.findViewById(R.id.dailyBarChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));

        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);

        return root;
    }

    public Cartesian createColumnChart(){
        //***** Read data from SQLiteDatabase *********
        stepsByDay = StepAppOpenHelper.loadStepsByDay(getContext(), current_time);

        // Creating a new map that contains days of the week and
        //  number of steps during each day set to 0
        Map<String, Integer> graph_map = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : stepsByDay.entrySet())
            graph_map.put(entry.getKey(), 0);

        // Replace the number of steps for each hour in graph_map
        //  with the number of steps read from the database
        graph_map.putAll(stepsByDay);

        //***** Create column chart using AnyChart library *********/
        // Create and get the cartesian coordinate system for column chart
        Cartesian cartesian = AnyChart.column();

        // Create data entries for x and y axis of the graph
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        //Add the data to column chart and get the columns
        Column column = cartesian.column(data);

        // Change the color of column chart and its border
        column.fill("#1EB980");
        column.stroke("#1EB980");

        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);


        // Modify the UI of the cartesian
        cartesian.yAxis(0).title("Number of steps");
        cartesian.xAxis(0).title("Days");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }
}
