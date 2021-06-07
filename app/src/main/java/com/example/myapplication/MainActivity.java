package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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

import java.util.Calendar;
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
            tv_proximity, tv_accelerator, tv_magnetic, tv_AID, tv_AAID, tv_data, tv_data_s;

    String device_AID;

//    String account_AAID;
//    Boolean isLimitedTrackingOn;
//    String providerPackage;
//    ListenableFuture<AdvertisingIdInfo> listenableFutureAAID;

    // as a global var this doesn't break...
    Button b_readFile, b_sendData;

    // The heart and soul of this app
    FusedLocationProviderClient fusedLocationProviderClient;

    // tracking location
//    boolean updateOn = false;

    // for config
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Storage
    StoreInfo storage;
    StoreSensorInfo sensorStorage;

    // Sensors
    SensorManager sensorManager;
    SensorEventListener sensorEventListener;
    Sensor sensorTemp;
    Sensor sensorHumidity;
    Sensor sensorPressure;
    Sensor sensorProximity;
    Sensor sensorLight;
    Sensor sensorAcceleration;
    Sensor sensorMagnetic;
    // helpers for sensors to reduce the updating
    int chill_pressure = 0;
    int chill_acl = 0;
    int chill_mag = 0;

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

        // Getting Device ID: reference https://www.youtube.com/watch?v=6tyGaqV2Gy0
        // Android does not include this in their documentation -- getting this info from a youtube search might help the arguments
        tv_AID = findViewById(R.id.tv_AID);
        // studio is telling me using "getString" to get Android ID is not recommended
        device_AID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        tv_AID.setText(device_AID);
        loadTheAAID();

        // Sensors
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorPressure  = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                // initialization of the singleton
                if (sensorStorage == null) {
                    sensorStorage = StoreSensorInfo.getInstance(device_AID);
                }
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_AMBIENT_TEMPERATURE :
                        // I do not have this sensor, however, leaving it here for the concept
                        String tempValue = event.values[0] + " °C";
                        tv_temp.setText(tempValue);
                        sensorStorage.updateData("Ambient Temperature", tempValue);
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY :
                        // same as above
                        String humidityValue = event.values[0] + "%";
                        tv_humidity.setText(humidityValue);
                        sensorStorage.updateData("Relative Humidity", humidityValue);
                        break;
                    case Sensor.TYPE_PRESSURE :
                        if (chill_pressure != 0) {
                            if (chill_pressure > 60) {
                                chill_pressure = 0;
                                break;
                            }
                            chill_pressure += 1;
                            break;
                        }
                        chill_pressure += 1;
                        String pressureValue = event.values[0] + " hPa";
                        tv_pressure.setText(pressureValue);
                        sensorStorage.updateData("Pressure", pressureValue);
                        break;
                    case Sensor.TYPE_PROXIMITY :
                        String proximityValue =  event.values[0] + " cm";
                        tv_proximity.setText(proximityValue);
                        sensorStorage.updateData("Proximity", proximityValue);
                        break;
                    case Sensor.TYPE_LIGHT :
                        String lightValue =  event.values[0] + " SI lux";
                        tv_light.setText(lightValue);
                        sensorStorage.updateData("Light", lightValue);
                        break;
                    case Sensor.TYPE_ACCELEROMETER :
                        if (chill_acl != 0) {
                            if (chill_acl > 60) {
                                chill_acl = 0;
                                break;
                            }
                            chill_acl += 1;
                            break;
                        }
                        chill_acl += 1;
                        String aclValue = "x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2];
                        tv_accelerator.setText(aclValue);
                        sensorStorage.updateData("Linear Acceleration", aclValue);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD :
                        if (chill_mag != 0) {
                            if (chill_mag > 60) {
                                chill_mag = 0;
                                break;
                            }
                            chill_mag += 1;
                            break;
                        }
                        chill_mag += 1;
                        String magneticValue = "x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2];
                        tv_magnetic.setText(magneticValue);
                        sensorStorage.updateData("Magnetic Field", magneticValue);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // place holder - don't think this is interesting atm
            }
        };

        sensorManager.registerListener(sensorEventListener, sensorAcceleration, SensorManager.SENSOR_DELAY_UI);
        //sensorManager.registerListener(sensorEventListener, sensorHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorMagnetic, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, sensorPressure, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(sensorEventListener, sensorTemp, SensorManager.SENSOR_DELAY_FASTEST);

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

        // TODO
        // Probably want to remove this as well and replace it with a non-UI function
        // There is no need to turn off location tracking from the UI so it might be best
        // only to remove the stopLocationUpdates()
        SwitchCompat locationSwitch = (SwitchCompat) findViewById(R.id.sw_locationsupdates);
        locationSwitch.setOnClickListener(v -> {
            if (locationSwitch.isChecked()) {
                startLocationUpdates();
                return;
            }
            stopLocationUpdates();
        });

        b_sendData = findViewById(R.id.b_sendData);
        b_sendData.setOnClickListener(v -> {
            if (!checkStorage()) {
                return;
            }
            // referencing https://stackoverflow.com/questions/2197741/how-to-send-emails-from-my-android-application/2197841#2197841
            Intent sendIntent = new Intent();
            String[] address = new String[]{"georgetownson39@gmail.com"};
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("message/rfc822");
            sendIntent.putExtra(Intent.EXTRA_EMAIL, address);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Test with " + storage.getID());
            sendIntent.putExtra(Intent.EXTRA_TEXT, storage.getData() + '\n' + sensorStorage.getData());

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            shareIntent.putExtra(Intent.EXTRA_CHOOSER_TARGETS, address);
            shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{sendIntent});
            startActivity(shareIntent);
        });

        b_readFile = findViewById(R.id.b_readFile);
        tv_data = findViewById(R.id.tv_data);
        tv_data_s = findViewById(R.id.tv_data_s);

        b_readFile.setOnClickListener(v -> {
            if (!checkStorage()) {
                tv_data.setText("Storage is null :(");
                tv_data_s.setText("Storage is null :(");
            }
            tv_data.setText(storage.getData());
            tv_data_s.setText(sensorStorage.getData());
        });

        updateGPS();
    }

    private boolean checkStorage() {
        if (storage == null || sensorStorage == null) {
            return false;
        }
        String test = storage.getData();
        String test2 = sensorStorage.getData();
        return test != null && test2 != null;
    }

    // might need this later for AAID...
//    private Runnable updateDataRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    };

    private void loadTheAAID() {
        tv_AAID.setText("Need SDKs to get AAID");
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

        if (storage == null) {
            storage = StoreInfo.getInstance(device_AID, cords, alt, speed, addy, confidence);
        } else storage.updateData(cords, alt, speed, addy, confidence);
    }

}