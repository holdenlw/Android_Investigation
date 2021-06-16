package com.example.myapplication;


import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StoreInfo {
    private static StoreInfo instance;

    private HashMap<String, ArrayList<String>> data;
    private String ID;

    private StoreInfo(String id) {
        ID = id;
        data = new HashMap<>();
        data.put("Coordinates", new ArrayList<>());
        data.put("Altitude", new ArrayList<>());
        data.put("Speed", new ArrayList<>());
        data.put("Address", new ArrayList<>());
        data.put("Accuracy", new ArrayList<>());
    }

    public static StoreInfo getInstance(String id, String cords, String alt, String speed, String address, String accuracy) {
        if (instance == null) {
            instance = new StoreInfo(id);
            instance.updateData(cords, alt, speed, address, accuracy);
        }
        return instance;
    }

    public String getID() { return ID; }

    public String getData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Location Data: ").append('\n');
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

    public void updateData(@NotNull String cords, @NotNull String alt, @NotNull String speed, @NotNull String address, @NotNull String accuracy) {
        data.get("Coordinates").add(cords);
        data.get("Altitude").add(alt);
        data.get("Speed").add(speed);
        data.get("Accuracy").add(accuracy);
        // Need to make sure address is not be repeated when it doesn't change
        // this might not be working
        if (data.get("Address").size() > 0) {
            if (!address.equals(data.get("Address").get(data.get("Address").size() - 1))) {
                data.get("Address").add(address);
            }
        } else {
            data.get("Address").add(address);
        }
    }



}
