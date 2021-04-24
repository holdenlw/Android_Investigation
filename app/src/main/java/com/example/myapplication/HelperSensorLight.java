package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

public class HelperSensorLight extends Application implements SensorEventListener {
    private Sensor lightSensor;
    private float lightValues;
    private SensorManager sensorManager;

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        lightValues = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getLight() {
        if (lightSensor == null) {
            return "No sensor";
        }
        return lightValues + "SI lux";
    }
}