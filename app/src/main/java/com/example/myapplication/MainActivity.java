package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
//import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;

//import androidx.ads.identifier.AdvertisingIdClient;
//import androidx.ads.identifier.AdvertisingIdInfo;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.provider.Settings;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

//import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.os.Build.*;
import static android.widget.Toast.makeText;
//import static com.google.android.gms.ads.identifier.AdvertisingIdClient.*;

// the sauce of the project: https://youtu.be/_xUcYfbtfsI
public class MainActivity extends AppCompatActivity {
    // Global constants
    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;

    // removing tv_updates and tv_sensor
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed,
            tv_address, tv_temp, tv_light, tv_pressure, tv_humidity,
            tv_proximity, tv_accelerator, tv_magnetic, tv_AID, tv_AAID, tv_popup;

    String device_AID;

//    String account_AAID;
//    Boolean isLimitedTrackingOn;
//    String providerPackage;
//    ListenableFuture<AdvertisingIdInfo> listenableFutureAAID;

    // Switches and buttons are for testing purposes
    SwitchCompat sw_sensors, sw_locations, sw_id;
    Button b_getData, b_readFile;

    // The heart and soul of this app
    FusedLocationProviderClient fusedLocationProviderClient;
    // tracking location
//    boolean updateOn = false;
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

    // Storage
    StoreInfo storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_address = findViewById(R.id.tv_address);
        tv_temp = findViewById(R.id.tv_temp);
        tv_light = findViewById(R.id.tv_light);
        tv_pressure = findViewById(R.id.tv_pressure);
        tv_humidity = findViewById(R.id.tv_humidity);
        tv_proximity = findViewById(R.id.tv_proximity);
        tv_accelerator = findViewById(R.id.tv_accelerator);
        tv_magnetic = findViewById(R.id.tv_magnetic);
        tv_AAID = findViewById(R.id.tv_AAID);

        // I learned about the "checked" feature of xml... makes the collection automatic
        sw_locations = findViewById(R.id.sw_locationsupdates);
        sw_sensors = findViewById(R.id.sw_sensors);
        sw_id = findViewById(R.id.sw_id);

        // switches for testing storage
        b_getData = findViewById(R.id.b_getData);
        b_readFile = findViewById(R.id.b_readFile);

        // Getting Device ID: reference https://www.youtube.com/watch?v=6tyGaqV2Gy0
        // Android does not include this in their documentation -- getting this info from a youtube search might help the arguments
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
        // We can keep this simple -- more likely to get errors if running too fast
        // how often default location request occurs
        // Highest priority setting -- BALANCED recommended
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL).setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValue(locationResult.getLastLocation());
            }
        };

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

        sw_id.setOnClickListener(v -> {
            if (sw_id.isChecked()) {
                loadTheAAID();
            } else tv_AAID.setText("Don't worry about this for now");
        });

        ConstraintLayout parent_layout = findViewById(R.id.cl_id);
        b_getData.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_main, null);
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupWindow.showAtLocation(parent_layout, Gravity.CENTER, 0, 0);

            tv_popup = findViewById(R.id.tv_popup);
            if (storage != null) {
                tv_popup.setText(storage.getData());
            }
            tv_popup.setText("Storage is null :/");

            Button closeButton = popupView.findViewById(R.id.b_close);
            closeButton.setOnClickListener(v1 -> popupWindow.dismiss());

        });

        b_readFile.setOnClickListener(v -> {
            // do something
        });

        updateGPS();
    }

    // will need this later...
//    private Runnable updateDataRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    };

    private void loadTheAAID() {
        tv_AAID.setText("Getting the AD ID is awful");
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
        tv_temp.setText(R.string.tv_temp);
        tv_light.setText(R.string.tv_light);
        tv_pressure.setText(R.string.tv_pressure);
        tv_humidity.setText(R.string.tv_humidity);
        tv_proximity.setText(R.string.tv_proximity);
        tv_accelerator.setText(R.string.tv_accelerator);
        tv_magnetic.setText(R.string.tv_magnetic);
    }

    // Turning off sensors and location tracking -- no longer needed but leaving it here
    private void stopLocationUpdates() {
        tv_lat.setText(R.string.tv_lat);
        tv_lon.setText(R.string.tv_lon);
        tv_accuracy.setText(R.string.tv_accuracy);
        tv_address.setText("Off");
        tv_speed.setText("Off");
        tv_altitude.setText("Off");
        // Studio is mad but this logic is stupid
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
//        tv_updates.setText("Location is being tracked");
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
        /* TODO: check this out... */
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
                makeText(this, "need permission to be granted to work", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS() {
        // get permissions, get location, then update UI
         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

             // ##### MAYBE ADD MORE HERE ##### //
             // Current location more likely to throw errors and is limited
             // Maybe getting the last location is just fine for our purposes
//             fusedLocationProviderClient.getCurrentLocation(priority int and CancellationToken).addOnSuccessListener(this,
//                     new OnSuccessListener<Location>() {
//                         @Override
//                         public void onSuccess(Location location) {
//                             updateUIValue(location);
//                         }
//                     });

             fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                     this::updateUIValue);
         } else {
             if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
             }
         }
    }

    private void updateUIValue(@NotNull Location location) {
        String speed;
        String alt;
        String addy;
        String confidence = String.valueOf(location.getAccuracy());

        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(confidence);

        // not every phone has the sensors of interest
        if (location.hasAltitude()) {
            alt = String.valueOf(location.getAltitude());
            tv_altitude.setText(alt);
        } else {
            tv_altitude.setText(R.string.tv_altitude);
            alt = "No altitude";
        }

        // also a speed accuracy sensor to check out
        if (location.hasSpeed()) {
            speed = String.valueOf(location.getSpeed());
            tv_speed.setText(speed);
        } else {
            tv_speed.setText(R.string.tv_speed);
            speed = "No speed";
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            addy = addresses.get(0).getAddressLine(0);
            tv_address.setText(addy);

        } catch (Exception e) {
            tv_address.setText(R.string.tv_address);
            addy = "No address";
        }

        String cords = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
        // a little ugly but it will do
        if (storage == null) {
            storage = new StoreInfo(device_AID, cords, alt, speed, addy, confidence);
        } else storage.updateData(cords, alt, speed, addy, confidence);
    }


}