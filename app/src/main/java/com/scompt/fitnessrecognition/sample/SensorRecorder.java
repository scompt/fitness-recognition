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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class SensorRecorder {

    private static final String LOG_TAG = SensorRecorder.class.getSimpleName();
    private static final IntentFilter BATTERY_CHANGED_FILTER =
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    private final SensorManager mSensorManager;
    private Context mContext;
    private File file;
    private ObjectOutputStream outputStream;
    private float[] mCachedPoints = new float[CACHED_TIMESTAMP_COUNT * POINTS_PER_TIMESTAMP];
    private long[] mCachedTimestamps = new long[CACHED_TIMESTAMP_COUNT];
    private int mCacheOffset;

    private static final int POINTS_PER_TIMESTAMP = 3;
    private static final int CACHED_TIMESTAMP_COUNT = 10;

    public SensorRecorder(Context context, File file) {
        mContext = context;
        this.file = file;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() throws IOException {
        outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        recordBatteryInformation();

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() throws IOException {
        mSensorManager.unregisterListener(mListener);

        recordBatteryInformation();
        outputStream.close();
    }

    private void recordBatteryInformation() throws IOException {
        Intent batteryIntent = mContext.registerReceiver(null, BATTERY_CHANGED_FILTER);
        if (batteryIntent == null) {
            Log.w(LOG_TAG, "batteryIntent was null");
            return;
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if(level != -1 && scale != -1) {
            outputStream.writeInt(1);
            float batteryPercentage = ((float) level / (float) scale) * 100.0f;
            outputStream.writeFloat(batteryPercentage);
        }
    }

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mCachedTimestamps[mCacheOffset] = event.timestamp;
            mCachedPoints[mCacheOffset * POINTS_PER_TIMESTAMP    ] = event.values[0];
            mCachedPoints[mCacheOffset * POINTS_PER_TIMESTAMP + 1] = event.values[1];
            mCachedPoints[mCacheOffset * POINTS_PER_TIMESTAMP + 2] = event.values[2];

            try {
                if (++mCacheOffset == CACHED_TIMESTAMP_COUNT) {
                    outputStream.writeInt(0);
                    for (int i = 0; i < mCacheOffset; i++) {
                        outputStream.writeLong(mCachedTimestamps[i]);
                        outputStream.writeFloat(mCachedPoints[i * POINTS_PER_TIMESTAMP]);
                        outputStream.writeFloat(mCachedPoints[i * POINTS_PER_TIMESTAMP + 1]);
                        outputStream.writeFloat(mCachedPoints[i * POINTS_PER_TIMESTAMP + 2]);
                    }
                    mCacheOffset = 0;
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
