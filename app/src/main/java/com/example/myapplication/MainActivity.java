package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.ads.identifier.AdvertisingIdInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.provider.Settings;
import android.view.View;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.os.Build.*;

// the sauce of the project: https://youtu.be/_xUcYfbtfsI
public class MainActivity extends AppCompatActivity {
    // the cool thing to do is set global constants
    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;

    // considering moving sensor specific variables to helper
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed,
            tv_sensor, tv_updates, tv_address, tv_temp, tv_light,
            tv_pressure, tv_humidity, tv_proximity, tv_accelerator, tv_magnetic, tv_AID;

    String device_AID;

    // TODO: remove switches, keep functionality
    Switch sw_locations, sw_gps, sw_sensors;

    // The heart and soul of this app
    FusedLocationProviderClient fusedLocationProviderClient;
    // tracking location
    boolean updateOn = false;
    // for config
    LocationRequest locationRequest;

    LocationCallback locationCallback;

    // Sensors
    HelperSensorTemp tempSensor;
    HelperSensorHumidity humiditySensor;
    HelperSensorPressure pressureSensor;
    HelperSensorProximity proximitySensor;
    HelperSensorLight lightSensor;
    HelperSensorAcceleration accelerationSensor;
    HelperSensorMagnetic magneticSensor;


//    public static ListenableFuture<AdvertisingIdInfo> getAdvertisingIdInfo(Context context) {
//        return null;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_temp = findViewById(R.id.tv_temp);
        tv_light = findViewById(R.id.tv_light);
        tv_pressure = findViewById(R.id.tv_pressure);
        tv_humidity = findViewById(R.id.tv_humidity);
        tv_proximity = findViewById(R.id.tv_proximity);
        tv_accelerator = findViewById(R.id.tv_accelerator);
        tv_magnetic = findViewById(R.id.tv_magnetic);

        sw_gps = findViewById(R.id.sw_gps);
        sw_locations = findViewById(R.id.sw_locationsupdates);
        sw_sensors = findViewById(R.id.sw_sensors);

        // Getting Device ID: reference https://www.youtube.com/watch?v=6tyGaqV2Gy0
        tv_AID = findViewById(R.id.tv_AID);
        // studio is telling me using "getString" to get Android ID is not recommended
        device_AID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        tv_AID.setText(device_AID);

        // sensors
        tempSensor = new HelperSensorTemp();
        humiditySensor = new HelperSensorHumidity();
        pressureSensor = new HelperSensorPressure();
        proximitySensor = new HelperSensorProximity();
        lightSensor = new HelperSensorLight();
        accelerationSensor = new HelperSensorAcceleration();
        magneticSensor = new HelperSensorMagnetic();

        // set properties of request
        locationRequest = LocationRequest.create();

        // how often default location request occurs
        // We can keep this simple -- more likely to get errors if running too fast
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        // how often request occurs at highest frequency
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);

        // NOTE: studio says I should access via the class, not the instance...
        // Highest priority setting -- BALANCED recommended
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save location
                updateUIValue(locationResult.getLastLocation());
            }
        };

        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Using GPS");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Using Cell Towers + WiFi");
            }
        });

        sw_locations.setOnClickListener(v -> {
            if (sw_locations.isChecked()) {
                startLocationUpdates();
            } else stopLocationUpdates();
        });

        sw_sensors.setOnClickListener(v -> {
            if (sw_sensors.isChecked()) {
                updateSensors();
            } else turnOffSensors();
        });

        updateGPS();
        updateSensors();

        // adding AAID

    }

    private void updateSensors() {
        // use getWorking() for testing
        tv_temp.setText(tempSensor.getTempValues());
        tv_humidity.setText(humiditySensor.getRelativeHumidity());
        tv_pressure.setText(pressureSensor.getPressure());
        tv_proximity.setText(proximitySensor.getProximity());
        tv_light.setText(lightSensor.getLight());
        tv_accelerator.setText(accelerationSensor.getAcceleration());
        tv_magnetic.setText(magneticSensor.getMagneticField());

    }

    private void turnOffSensors() {
        tv_temp.setText("Off");
        tv_light.setText("Off");
        tv_pressure.setText("Off");
        tv_humidity.setText("Off");
        tv_proximity.setText("Off");
        tv_accelerator.setText("Off");
        tv_magnetic.setText("Off");
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText("Off");
        tv_lon.setText("Off");
        tv_accuracy.setText("Off");
        tv_address.setText("Off");
        tv_speed.setText("Off");
        tv_altitude.setText("Off");
        tv_sensor.setText("Off");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

            // yeah idk about what is going on up here
        }
        // TODO: check this out...
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "need permission to be granted to work", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS() {
        // get permissions, get location, then update UI
         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

             // ##### MAYBE ADD MORE HERE ##### //
             // Current location more likely to throw errors and is limited to
//             fusedLocationProviderClient.getCurrentLocation(priority int and CancellationToken).addOnSuccessListener(this,
//                     new OnSuccessListener<Location>() {
//                         @Override
//                         public void onSuccess(Location location) {
//                             updateUIValue(location);
//                         }
//                     });
             //location -> updateUIValue(location)); --> replaced
             fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                     this::updateUIValue);
         } else {
             if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
             }
         }
    }

    private void updateUIValue(@NotNull Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        // not every phone has the sensors of interest
        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else tv_altitude.setText("No Altitude Sensor");

        // also a speed accuracy sensor to check out
        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else tv_speed.setText("No Speed Sensor or no speed detected");

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            // lots of value in this data
            tv_address.setText(addresses.get(0).getAddressLine(0));

        } catch (Exception e) {
            tv_address.setText("Unable to get address");
        }
    }


}