package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

public class HelperSensorProximity extends Application implements SensorEventListener {
    private Sensor proxSensor;
    private float proxValues;
    private SensorManager sensorManager;

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        proxValues = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getProximity() {
        if (proxSensor == null) {
            return "No sensor";
        }
        return proxValues + "cm";
    }
}