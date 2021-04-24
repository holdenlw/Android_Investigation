package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.CallSuper;

public class HelperSensorTemp extends Application implements SensorEventListener {
    private Sensor tempSensor;
    private float tempValues;
    private SensorManager sensorManager;
//    private boolean working; makeshift debugging process for sensors

    @CallSuper
    public void onCreate() {
        super.onCreate();
        // sensorManger recommended to be a local object but that also is advised against!

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        if (tempSensor != null) {
//            working = true;
//        } else {
//            working = false;
//        }

// Just going to assume there is a sensor
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
//            tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//            sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        } else tempSensor = null;

    }

    @Override
    public void onSensorChanged(@org.jetbrains.annotations.NotNull SensorEvent event) {
        tempValues = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getTempValues() {
        if (tempSensor == null) {
            return "No sensor";
        }
        return tempValues + "°C";
    }
}
//    public String getWorking() {
//        if (working) {
//            return "this is weird";
//        }
//        return "this isn't working";
//    }

