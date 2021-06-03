package com.example.myapplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class StoreSensorInfo {
    private static StoreSensorInfo instance;

    private HashMap<String, ArrayList<String>> data;
    private String ID;

    private StoreSensorInfo(String id) {
        ID = id + Calendar.getInstance().toString();
        data = new HashMap<>();
        data.put("", new ArrayList<>());
        data.put("", new ArrayList<>());
        data.put("", new ArrayList<>());
        data.put("", new ArrayList<>());
        data.put("", new ArrayList<>());
    }

    public static StoreSensorInfo getInstance(String id, String cords, String alt, String speed, String address, String accuracy) {
        if (instance == null) {
            instance = new StoreSensorInfo(id);
            instance.updateData(cords, alt, speed, address, accuracy);
        }
        return instance;
    }

    public String getID() { return ID; }

    public String getData() {
        // TODO
        //  make this more readable
        return data.toString();
    }

    public void updateData(String cords, String alt, String speed, String address, String accuracy) {
        data.get("Coordinates").add(cords);
        data.get("Altitude").add(alt);
        data.get("Speed").add(speed);
        data.get("Address").add(address);
        data.get("Confidence").add(accuracy);
    }
}
