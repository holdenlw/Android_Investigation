package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

import java.util.Arrays;

public class HelperSensorMagnetic extends Application implements SensorEventListener {
    private Sensor magSensor;
    private float[] magValues;
    private SensorManager sensorManager;

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        magValues[0] = event.values[0]; // x-axis
        magValues[1] = event.values[1]; // y-axis
        magValues[2] = event.values[2]; // z-axis
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getMagneticField() {
        if (magSensor == null) {
            return "No sensor";
        }
        return "I got to do the physics" + Arrays.toString(magValues);
    }
}