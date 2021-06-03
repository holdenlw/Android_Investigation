package com.example.myapplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StoreSensorInfo {
    private static StoreSensorInfo instance;

    private HashMap<String, ArrayList<String>> data;
    private String ID;

    private StoreSensorInfo(String id) {
        ID = id + Calendar.getInstance().toString();
        data = new HashMap<>();
        data.put("Ambient Temperature", new ArrayList<>());
        data.put("Relative Humidity", new ArrayList<>());
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
        stringBuilder.append("              Sensor Data               ").append('\n');
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

    // you can only get information when the sensor changes
    public void updateData(String changed, String value) {
        switch (changed) {
            case "temp" :
                data.get("Ambient Temperature").add(value);
                break;
            case "humid" :
                data.get("Relative Humidity").add(value);
                break;
            case "light" :
                data.get("Light").add(value);
                break;
            case "pres" :
                data.get("Pressure").add(value);
                break;
            case "prox" :
                data.get("Proximity").add(value);
                break;
            case "acl" :
                data.get("Linear Acceleration").add(value);
                break;
            case "mag" :
                data.get("Magnetic Field").add(value);
                break;
            default:
                break;
        }
    }
    // sensors seem to be crazy on updating so might need to add this function
    private Boolean checkForValueChange(String key, String value) {
        return false;
    }

}
