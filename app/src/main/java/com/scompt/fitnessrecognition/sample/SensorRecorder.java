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

import com.scompt.fitnessrecognition.common.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import static com.scompt.fitnessrecognition.sample.Constants.SENSOR_DELAY;
import static com.scompt.fitnessrecognition.sample.Constants.SENSOR_TYPE;

public class SensorRecorder {

    private static final String LOG_TAG = SensorRecorder.class.getSimpleName();
    private static final IntentFilter BATTERY_CHANGED_FILTER =
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    private final SensorManager mSensorManager;
    private Context mContext;
    private File file;
    private ObjectOutputStream outputStream;
    private float[] mCachedPoints = new float[Constants.CHUNK_SIZE * POINTS_PER_TIMESTAMP];
    private long[] mCachedTimestamps = new long[Constants.CHUNK_SIZE];
    private int mCacheOffset;

    private static final int POINTS_PER_TIMESTAMP = 3;

    public SensorRecorder(Context context, File file) {
        mContext = context;
        this.file = file;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() throws IOException {
        outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        recordBatteryInformation();

        List<Sensor> sensors = mSensorManager.getSensorList(SENSOR_TYPE);
        mSensorManager.registerListener(mListener, sensors.get(0), SENSOR_DELAY);
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
            outputStream.writeInt(Constants.BATTERY_CHUNK_TYPE);
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
                if (++mCacheOffset == Constants.CHUNK_SIZE) {
                    outputStream.writeInt(Constants.DATA_CHUNK_TYPE);
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
