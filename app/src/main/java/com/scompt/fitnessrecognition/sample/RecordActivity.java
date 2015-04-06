package com.scompt.fitnessrecognition.sample;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RecordActivity extends Activity implements DelayedConfirmationView.DelayedConfirmationListener {

    private static final int MAX_TRACES = 100;
    private static final String LOG_TAG = RecordActivity.class.getSimpleName();

    private TextView mTextView;

    private DelayedConfirmationView mDelayedConfirmationView;

    private Button mStartButton;

    private File mTraceFile;
    private int mTraceNumber;
    private SensorRecorder sensorRecorder;

    private enum State { IDLE, COUNTING_DOWN, RECORDING }

    private State mState = State.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mStartButton = (Button) stub.findViewById(R.id.start_button);

                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startCountdown();
                    }
                });


                mDelayedConfirmationView = (DelayedConfirmationView) stub.findViewById(R.id.delayed_confirm);
                mDelayedConfirmationView.setListener(RecordActivity.this);

                getTraceFile();
            }
        });
    }

    private void startCountdown() {
        mState = State.COUNTING_DOWN;
        mStartButton.setVisibility(View.INVISIBLE);
        mDelayedConfirmationView.setStartTimeMs(0);
        mDelayedConfirmationView.setTotalTimeMs(5000);
        mDelayedConfirmationView.start();

//        mDelayedConfirmationView.setVisibility(View.VISIBLE);
    }

    private File getDataDirectory() {
        File docsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dataDirectory = new File(docsDirectory, "fitness_recognition");

        if (dataDirectory.mkdirs() || dataDirectory.isDirectory()) {
            return dataDirectory;
        } else {
            throw new RuntimeException();
            // TODO: show error message
        }
    }

    private void getTraceFile() {
        File dataDirectory = getDataDirectory();

        int traceNumber = 0;
        while (traceNumber < MAX_TRACES) {
            String filename = "trace_" + traceNumber;
            File traceFile = new File(dataDirectory, filename);
            if (!traceFile.exists()) {
                mTextView.setText("Trace #" + traceNumber);
                mTraceFile = traceFile;
                mTraceNumber = traceNumber;
                return;
            }
            traceNumber++;
        }

        // TODO: show error message
    }

    @Override
    public void onTimerFinished(View view) {
        if (mState == State.COUNTING_DOWN) {
            startRecording();
        } else if (mState == State.RECORDING) {
            finishRecording();
        }
    }

    private void finishRecording() {
        try {
            sensorRecorder.stop();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            sensorRecorder = null;
        }

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Recorded Trace #" + mTraceNumber);
        startActivity(intent);
        finish();
    }


    private void startRecording() {
        mState = State.RECORDING;
        mDelayedConfirmationView.setImageResource(android.R.drawable.checkbox_on_background);
        mDelayedConfirmationView.setStartTimeMs(0);
        mDelayedConfirmationView.setTotalTimeMs(30000);
        mDelayedConfirmationView.reset();
        mDelayedConfirmationView.start();

        sensorRecorder = new SensorRecorder(this, mTraceFile);
        try {
            sensorRecorder.start();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onTimerSelected(View view) {
        mDelayedConfirmationView.reset();
        if (mState == State.COUNTING_DOWN) {
            cancelCountdown();
        } else if (mState == State.RECORDING) {
            finishRecording();
        }
    }

    private void cancelCountdown() {
        mDelayedConfirmationView.reset();
//            mDelayedConfirmationView.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
    }

}
