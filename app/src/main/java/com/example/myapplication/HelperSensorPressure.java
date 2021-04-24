package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

public class HelperSensorPressure extends Application implements SensorEventListener {
    private Sensor pressSensor;
    private float pressValues;
    private SensorManager sensorManager;

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, pressSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        pressValues = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getPressure() {
        if (pressSensor == null) {
            return "No sensor";
        }
        return pressValues + "hPa";
    }
}