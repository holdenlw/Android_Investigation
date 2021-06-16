package com.example.myapplication;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StoreSensorInfo {
    private static StoreSensorInfo instance;

    private HashMap<String, ArrayList<String>> data;
    private String ID;

    private StoreSensorInfo(String id) {
        ID = id + " at " + Calendar.getInstance().toString();
        data = new HashMap<>();
        //data.put("Ambient Temperature", new ArrayList<>());
        //data.put("Relative Humidity", new ArrayList<>());
        data.put("Light", new ArrayList<>());
        data.put("Pressure", new ArrayList<>());
        data.put("Proximity", new ArrayList<>());
        data.put("Linear Acceleration", new ArrayList<>());
        data.put("Magnetic Field", new ArrayList<>());

    }

    public static StoreSensorInfo getInstance(String id) {
        if (instance == null) {
            instance = new StoreSensorInfo(id);
        }
        return instance;
    }

    public String getID() { return ID; }

    public String getData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sensor Data: ").append('\n');
        for (Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ");
            ArrayList<String> list = entry.getValue();
            for (String s : list) {
                if (list.indexOf(s) == list.size() - 1) {
                    stringBuilder.append(s);
                    break;
                }
                stringBuilder.append(s).append(", ");
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    // excluding linear acceleration and magnetic field for now
    public void updateData(@NotNull String key, @NotNull String value) {
        data.get(key).add(value);
    }

}
