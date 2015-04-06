package com.scompt.fitnessrecognition.sample;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by scompt on 11/04/15.
 */
public class SensorRecorder {

    private static final String LOG_TAG = SensorRecorder.class.getSimpleName();

    private final SensorManager mSensorManager;
    private Context mContext;
    private File file;
    private ObjectOutputStream outputStream;
    private FileWriter fileWriter;

    public SensorRecorder(Context context, File file) {
        mContext = context;
        this.file = file;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

    }

    public void start() throws IOException {
//        outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        fileWriter = new FileWriter(file);
        recordBatteryInformation();

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() throws IOException {
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.unregisterListener(mListener);

        recordBatteryInformation();
        fileWriter.close();
    }

    private void recordBatteryInformation() throws IOException {
        Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            fileWriter.write("-1\n");

        } else {
            float batteryPercentage = ((float) level / (float) scale) * 100.0f;
            fileWriter.write(String.valueOf(batteryPercentage));
            fileWriter.write('\n');
        }
    }

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
/*
                outputStream.writeLong(event.timestamp);
                outputStream.writeFloat(event.values[0]);
                outputStream.writeFloat(event.values[1]);
                outputStream.writeFloat(event.values[2]);
*/
                fileWriter.write(String.valueOf(event.timestamp));
                fileWriter.write(',');
                fileWriter.write(String.valueOf(event.values[0]));
                fileWriter.write(',');
                fileWriter.write(String.valueOf(event.values[1]));
                fileWriter.write(',');
                fileWriter.write(String.valueOf(event.values[2]));
                fileWriter.write('\n');

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

}
