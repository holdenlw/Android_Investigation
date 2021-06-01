package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

import java.util.Arrays;

public class HelperSensorAcceleration implements SensorEventListener {
    private Sensor accelSensor;
    private float[] accelValues;
    private SensorManager sensorManager;

    private static HelperSensorAcceleration instance;

    private HelperSensorAcceleration(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    public static HelperSensorAcceleration getInstance(Context context) {
        if (instance == null) {
            instance = new HelperSensorAcceleration(context);
        }
        return instance;
    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        accelValues[0] = event.values[0]; // x-axis
        accelValues[1] = event.values[1]; // y-axis
        accelValues[2] = event.values[2]; // z-axis
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getAcceleration() {
        if (accelSensor == null) {
            return "No sensor";
        }
        return Arrays.toString(accelValues);
    }
}