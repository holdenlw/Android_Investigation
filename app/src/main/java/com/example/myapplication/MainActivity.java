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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION;
import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {
    // Global constants
    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int CHILL_FACTOR = 100;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed,
            tv_address, tv_temp, tv_light, tv_pressure, tv_humidity,
            tv_proximity, tv_accelerator, tv_magnetic, tv_AID, tv_AAID, tv_data, tv_data_s;

    String device_AID, account_AAID;

    Button b_readFile, b_sendData;

    // The heart and soul of this app
    FusedLocationProviderClient fusedLocationProviderClient;

    // for config
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Storage
    StoreInfo storage;
    StoreSensorInfo sensorStorage;

    // Sensors
    SensorManager sensorManager;
    SensorEventListener sensorEventListener;
    Sensor sensorTemp, sensorHumidity, sensorPressure, sensorProximity, sensorLight, sensorAcceleration, sensorMagnetic;
    // helpers for sensors to reduce the updating
//    int chill_pressure = 0;
//    int chill_acl = 0;
//    int chill_mag = 0;

    // Conditions
    RadioButton rb_a_0, rb_a_1, rb_s_0, rb_s_1, rb_s_2, rb_e_0, rb_e_1, rb_e_2, rb_e_3;
    HashMap<String, String> conditions;
    long start_time;

//    String debug = "idk";

    @RequiresApi(api = Build.VERSION_CODES.N)
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

        rb_a_0 = findViewById(R.id.rb_a_0);
        rb_a_1 = findViewById(R.id.rb_a_1);
        rb_s_0 = findViewById(R.id.rb_s_0);
        rb_s_1 = findViewById(R.id.rb_s_1);
        rb_s_2 = findViewById(R.id.rb_s_2);
        rb_e_0 = findViewById(R.id.rb_e_0);
        rb_e_1 = findViewById(R.id.rb_e_1);
        rb_e_2 = findViewById(R.id.rb_e_2);
        rb_e_3 = findViewById(R.id.rb_e_3);

        conditions = new HashMap<>();
        start_time = System.currentTimeMillis();

        // Getting Device ID and AAID: reference https://www.youtube.com/watch?v=6tyGaqV2Gy0 and https://proandroiddev.com/how-to-generate-android-unique-id-38362794e1a8
        // Android does not include this in their documentation -- getting this info from a youtube search might help the arguments -- its super easy
        tv_AID = findViewById(R.id.tv_AID);
        // studio is telling me using "getString" to get Android ID is not recommended
        device_AID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        tv_AID.setText(device_AID);

        // eh not worth it - pretty much serves the same function as above 
//        tv_DID = findViewById(R.id.tv_DID);
//        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
//        device_ID = telephonyManager.getDeviceId();
//        tv_DID.setText(device_ID);

        Runnable idRunnable = this::loadTheAAID;
        Thread idThread = new Thread(idRunnable);
        idThread.start();

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
                    sensorStorage = StoreSensorInfo.getInstance(device_AID + " and " + account_AAID);
                }
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_AMBIENT_TEMPERATURE :
                        // I do not have this sensor, however, leaving it here for the concept
                        String tempValue = event.values[0] + " °C";
                        tv_temp.setText(tempValue);
                        sensorStorage.updateData("Ambient Temperature", String.valueOf(event.values[0]));
                        break;
                    case Sensor.TYPE_RELATIVE_HUMIDITY :
                        // same as above
                        String hValue = event.values[0] + "%";
                        tv_humidity.setText(hValue);
                        sensorStorage.updateData("Relative Humidity", String.valueOf(event.values[0]));
                        break;
                    case Sensor.TYPE_PRESSURE :
//                        if (chill_pressure != 0) {
//                            if (chill_pressure > CHILL_FACTOR) {
//                                chill_pressure = 0;
//                                break;
//                            }
//                            chill_pressure += 1;
//                            break;
//                        }
//                        chill_pressure += 1;
                        String pressureValue = event.values[0] + " hPa";
                        tv_pressure.setText(pressureValue);
                        sensorStorage.updateData("Pressure", String.valueOf(event.values[0]));
                        break;
                    case Sensor.TYPE_PROXIMITY :
                        String proximityValue =  event.values[0] + " cm";
                        tv_proximity.setText(proximityValue);
                        sensorStorage.updateData("Proximity", String.valueOf(event.values[0]));
                        break;
                    case Sensor.TYPE_LIGHT :
                        String lightValue =  event.values[0] + " SI lux";
                        tv_light.setText(lightValue);
                        sensorStorage.updateData("Light", String.valueOf(event.values[0]));
                        break;
                    case Sensor.TYPE_ACCELEROMETER :
//                        if (chill_acl != 0) {
//                            if (chill_acl > CHILL_FACTOR) {
//                                chill_acl = 0;
//                                break;
//                            }
//                            chill_acl += 1;
//                            break;
//                        }
//                        chill_acl += 1;
                        String aclValue = "x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2];
                        tv_accelerator.setText(aclValue);
                        sensorStorage.updateData("Linear Acceleration", "(" + aclValue + ")");
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD :
//                        if (chill_mag != 0) {
//                            if (chill_mag > CHILL_FACTOR) {
//                                chill_mag = 0;
//                                break;
//                            }
//                            chill_mag += 1;
//                            break;
//                        }
//                        chill_mag += 1;
                        String magneticValue = "x: " + event.values[0] + ", y: " + event.values[1] + ", z: " + event.values[2];
                        tv_magnetic.setText(magneticValue);
                        sensorStorage.updateData("Magnetic Field", "(" + magneticValue + ")");
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
        // how often default location request occur; Highest priority setting -- BALANCED recommended
        //
        // change from high priority to balanced ==> high prority set when fine location acess gratned
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL).setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValue(locationResult.getLastLocation());
            }
        };
        /* Note
        The only purpose this switch servers is automatically turning on location tracking.
        It might be best practice to remove or replace this way of doing it, but not worth it for now
         */
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
            if (checkStorage()) {
                return;
            }
            long milli = System.currentTimeMillis() - start_time;
            int secs = (int) (milli / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            String duration = String.format("%d:%02d", mins, secs);
            if (conditions.containsKey("Duration")) {
                conditions.replace("Duration", duration);
            } else {
                conditions.put("Duration", duration);
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Conditions").append('\n');
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
            }

            // referencing https://stackoverflow.com/questions/2197741/how-to-send-emails-from-my-android-application/2197841#2197841
            Intent sendIntent = new Intent();
            String[] address = new String[]{"georgetownson39@gmail.com"};
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("message/rfc822");
            sendIntent.putExtra(Intent.EXTRA_EMAIL, address);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, Calendar.getInstance().getTime().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Test with " + storage.getID()+ '\n'
                    + stringBuilder.toString() + '\n' + storage.getData() + '\n' + sensorStorage.getData());

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            shareIntent.putExtra(Intent.EXTRA_CHOOSER_TARGETS, address);
            shareIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{sendIntent});
            startActivity(shareIntent);
        });

        /* This section is for testing - can be removed on deployment */
        b_readFile = findViewById(R.id.b_readFile);
        tv_data = findViewById(R.id.tv_data);
        tv_data_s = findViewById(R.id.tv_data_s);

        b_readFile.setOnClickListener(v -> {
            if (checkStorage()) {
                tv_data.setText("Storage is null :(");
                tv_data_s.setText("Storage is null :(");
            }
            tv_data.setText(storage.getData());
        });

        updateGPS();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onConditionClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int v_id = view.getId();
        // have to use if/else instead of switch because ID is non-final
        // this is an ugly performance beast
        if (v_id == R.id.rb_a_0) {
            if (checked) {
                if (conditions.containsKey("Age")) {
                    conditions.replace("Age", "Default");
                } else {
                    conditions.put("Age", "Default");
                }
            }
        } else if (v_id == R.id.rb_a_1) {
            if (checked) {
                if (conditions.containsKey("Age")) {
                    conditions.replace("Age", "Kid");
                } else {
                    conditions.put("Age", "Kid");
                }
            }
        } else if (v_id == R.id.rb_s_0) {
            if (checked) {
                if (conditions.containsKey("Settings")) {
                    conditions.replace("Settings", "Default");
                } else {
                    conditions.put("Settings", "Default");
                }
            }
        } else if (v_id == R.id.rb_s_1) {
            if (checked) {
                if (conditions.containsKey("Settings")) {
                    conditions.replace("Settings", "Privacy");
                } else {
                    conditions.put("Settings", "Privacy");
                }
            }
        } else if (v_id == R.id.rb_s_2) {
            if (checked) {
                if (conditions.containsKey("Settings")) {
                    conditions.replace("Settings", "SomePrivacy");
                } else {
                    conditions.put("Settings", "SomePrivacy");
                }
            }
        } else if (v_id == R.id.rb_e_0) {
            if (checked) {
                if (conditions.containsKey("Environment")) {
                    conditions.replace("Environment", "Sitting");
                } else {
                    conditions.put("Environment", "Sitting");
                }
            }
        } else if (v_id == R.id.rb_e_1) {
            if (checked) {
                if (conditions.containsKey("Environment")) {
                    conditions.replace("Environment", "Walking");
                } else {
                    conditions.put("Environment", "Walking");
                }
            }
        } else if (v_id == R.id.rb_e_2) {
            if (checked) {
                if (conditions.containsKey("Environment")) {
                    conditions.replace("Environment", "Driving");
                } else {
                    conditions.put("Environment", "Driving");
                }
            }
        } else if (v_id == R.id.rb_e_3) {
            if (checked) {
                if (conditions.containsKey("Environment")) {
                    conditions.replace("Environment", "Room2Room");
                } else {
                    conditions.put("Environment", "Room2Room");
                }
            }
        }

    }

    private boolean checkStorage() {
        if (storage == null || sensorStorage == null) {
            return true;
        }
        String test = storage.getData();
        String test2 = sensorStorage.getData();
        return test == null || test2 == null;
    }

    /* Saving grace of stack overflow: https://stackoverflow.com/questions/64087871/how-to-get-google-ad-id-in-android-java */
    private void loadTheAAID() {
        AdvertisingIdClient.Info idInfo;
        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
            if(idInfo.isLimitAdTrackingEnabled()) {
                account_AAID = "Privacy Enabled";
                tv_AAID.setText("Limited tracking enabled");
            } else {
                account_AAID = idInfo.getId();
                tv_AAID.setText(account_AAID);
            }
        } catch (IOException e) {
            String exception_explained = "IOException: " + e;
            tv_AAID.setText(exception_explained);
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            tv_AAID.setText("Google Play Services Not Available");
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            tv_AAID.setText("Google Play Repairable Exception");
            e.printStackTrace();
        }

    }

    // Turning off sensors and location tracking -- no longer needed but leaving it here
    private void stopLocationUpdates() {
        tv_lat.setText(R.string.tv_lat);
        tv_lon.setText(R.string.tv_lon);
        tv_accuracy.setText(R.string.tv_accuracy);
        tv_address.setText("Off");
        tv_speed.setText("Off");
        tv_altitude.setText("Off");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        /* Probably don't want to implement this error handling because we want to see the difference between
        the conditions where permissions are on and off
        */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            stopLocationUpdates();
            return;
            // yeah idk about what is going on up here
        }
        /* Might need to change this but it works for now */
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                updateGPS();
            } else {
//                makeText(this, "need permission to be granted to work", Toast.LENGTH_SHORT).show();
//                finish();
                stopLocationUpdates();
                if (storage == null) {
                    storage = StoreInfo.getInstance("device " + device_AID + " and account " + account_AAID, "null", "null", "null", "null", "null");
                }
            }
        }
    }

    private void updateGPS() {
        // get permissions, get location, then update UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
            tv_speed.setText("No Speed");
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
            storage = StoreInfo.getInstance("device " + device_AID + " and account " + account_AAID, cords, alt, speed, addy, confidence);
        } else storage.updateData(cords, alt, speed, addy, confidence);
    }

}