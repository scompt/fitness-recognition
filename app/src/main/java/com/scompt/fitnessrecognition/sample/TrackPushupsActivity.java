package com.scompt.fitnessrecognition.sample;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;

import com.scompt.fitnessrecognition.common.PeakDetProcessor;
import com.scompt.fitnessrecognition.common.Processor;

import java.util.List;

public class TrackPushupsActivity extends Activity {

    private TextView mTextView;
    private SensorManager mSensorManager;
    private final Processor mProcessor = new PeakDetProcessor(20);
    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            boolean isRepetition = mProcessor.processPoint(event.timestamp, event.values[0],
                    event.values[1], event.values[2]);

            if (isRepetition) {
                incrementReps();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private int mRepetitionCount;
    private Vibrator mVibrator;

    private void incrementReps() {
        mVibrator.vibrate(400);
        mTextView.setText(String.valueOf(++mRepetitionCount));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        mTextView = (TextView) findViewById(R.id.repetition_text_view);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mRepetitionCount = 0;
        mTextView.setText(String.valueOf(mRepetitionCount));
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mListener);
    }
}
