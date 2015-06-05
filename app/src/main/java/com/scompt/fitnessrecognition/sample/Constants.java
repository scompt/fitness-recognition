package com.scompt.fitnessrecognition.sample;

import android.hardware.Sensor;
import android.hardware.SensorManager;

public class Constants {
    public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;
    public static final int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;

    private Constants() {
        throw new AssertionError("No instances");
    }
}
