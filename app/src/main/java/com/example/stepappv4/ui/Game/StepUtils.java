package com.example.stepappv4.ui.Game;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.example.stepappv4.StepAppOpenHelper;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class  StepUtils implements SensorEventListener {

    private long lastSensorUpdate = 0;
    public static int accStepCounter = 0;
    ArrayList<Integer> accSeries = new ArrayList<Integer>();
    ArrayList<String> timestampsSeries = new ArrayList<String>();
    private double accMag = 0;
    private int lastAddedIndex = 1;
    int stepThreshold = 6;

    TextView stepCountsView;

    CircularProgressIndicator progressBar;
    private SQLiteDatabase database;

    private String timestamp;
    private String day;
    private String hour;


    public void StepCounterListener(TextView stepCountsView, CircularProgressIndicator progressBar, SQLiteDatabase databse)
    {
        this.stepCountsView = stepCountsView;
        this.database = databse;
        this.progressBar = progressBar;
    }

    public void StepCounterListener2() {}


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType())
        {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long currentTimeInMilliSecond = System.currentTimeMillis();

                long timeInMillis = currentTimeInMilliSecond + (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

                // Convert the timestamp to date
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String sensorEventDate = jdf.format(timeInMillis);




                if ((currentTimeInMilliSecond - lastSensorUpdate) > 1000)
                {
                    lastSensorUpdate = currentTimeInMilliSecond;
                    String sensorRawValues = "  x = "+ String.valueOf(x) +"  y = "+ String.valueOf(y) +"  z = "+ String.valueOf(z);
                    Log.d("Acc. Event", "last sensor update at " + String.valueOf(sensorEventDate) + sensorRawValues);
                }


                accMag = Math.sqrt(x*x+y*y+z*z);


                accSeries.add((int) accMag);

                // Get the date, the day and the hour
                timestamp = sensorEventDate;
                day = sensorEventDate.substring(0,10);
                hour = sensorEventDate.substring(11,13);

                Log.d("SensorEventTimestampInMilliSecond", timestamp);


                timestampsSeries.add(timestamp);
                peakDetection();

                break;

        }


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void peakDetection() {

        int windowSize = 20;
        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer Mladenov et al.
         */
        int currentSize = accSeries.size(); // get the length of the series
        if (currentSize - lastAddedIndex < windowSize) { // if the segment is smaller than the processing window size skip it
            return;
        }

        List<Integer> valuesInWindow = accSeries.subList(lastAddedIndex,currentSize);
        List<String> timePointList = timestampsSeries.subList(lastAddedIndex,currentSize);
        lastAddedIndex = currentSize;

        for (int i = 1; i < valuesInWindow.size()-1; i++) {
            int forwardSlope = valuesInWindow.get(i + 1) - valuesInWindow.get(i);
            int downwardSlope = valuesInWindow.get(i) - valuesInWindow.get(i - 1);

            if (forwardSlope < 0 && downwardSlope > 0 && valuesInWindow.get(i) > stepThreshold) {
                accStepCounter += 1;
                Log.d("ACC STEPS: ", String.valueOf(accStepCounter));
                stepCountsView.setText(String.valueOf(accStepCounter));
                progressBar.setProgress(accStepCounter);

                ContentValues databaseEntry = new ContentValues();
                databaseEntry.put(StepAppOpenHelper.KEY_TIMESTAMP, timePointList.get(i));

                databaseEntry.put(StepAppOpenHelper.KEY_DAY, this.day);
                databaseEntry.put(StepAppOpenHelper.KEY_HOUR, this.hour);

                database.insert(StepAppOpenHelper.TABLE_NAME, null, databaseEntry);

            }
        }
    }

}