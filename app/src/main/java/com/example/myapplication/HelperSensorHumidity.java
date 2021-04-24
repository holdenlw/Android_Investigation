package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

public class HelperSensorHumidity extends Application implements SensorEventListener {
    private Sensor humSensor;
    private float humValues;
    private SensorManager sensorManager;

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        humSensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorManager.registerListener(this, humSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        humValues = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getRelativeHumidity() {
        if (humSensor==null) {
            return "No sensor";
        }
        // code got angry at me for not doing this, but then got angry when I did the suggestion
        return humValues + "%";
    }

    // optional
//    public String getDewPoint() {
//
//    }
//    public String getAbsoluteHumidity() {
//
//    }

}
