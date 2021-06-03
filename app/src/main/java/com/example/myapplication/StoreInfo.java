package com.example.myapplication;


import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

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
        ID = id + Calendar.getInstance().toString();
        data = new HashMap<>();
        data.put("Coordinates", new ArrayList<>());
        data.put("Altitude", new ArrayList<>());
        data.put("Speed", new ArrayList<>());
        data.put("Address", new ArrayList<>());
        data.put("Confidence", new ArrayList<>());
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
        // TODO
        //  make this more readable
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Location Data: ").append('\n');
        for (Map.Entry<String, ArrayList<String>> entry : data.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ");
            ArrayList<String> list = entry.getValue();
            for (String s : list) {
                // this will add an extra comma but I will come back to this
                stringBuilder.append(s).append(", ");
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    public void updateData(String cords, String alt, String speed, String address, String accuracy) {
        data.get("Coordinates").add(cords);
        data.get("Altitude").add(alt);
        data.get("Speed").add(speed);
        data.get("Address").add(address);
        data.get("Confidence").add(accuracy);
    }


    // writing file and putting it into local storage
    public void writeFile(Context context) {
        try  {
            FileOutputStream fos = context.openFileOutput(ID, Context.MODE_PRIVATE);
            fos.write(data.toString().getBytes());
        } catch (IOException e) {
            Toast.makeText(context, "Can't write file" ,Toast.LENGTH_SHORT).show();
        }
    }

    // for now this is just testing that this all works
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String readFile(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(ID + ".txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Can't find file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "idk", Toast.LENGTH_SHORT).show();
        }
        return stringBuilder.toString();
    }

    /* I will not use this function for now, but it will be useful in the future */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void updateFile(Context context)  {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(ID + ".txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                // going crazy with if statements
                if (line.contains("Coordinates")) {
                    stringBuilder.append(line).append(", ").append(this.data.get("Coordinates")).append('\n');
                } else if (line.contains("Altitude")) {
                    stringBuilder.append(line).append(", ").append(this.data.get("Altitude")).append('\n');
                } else if (line.contains("Speed")) {
                    stringBuilder.append(line).append(", ").append(this.data.get("Speed")).append('\n');
                } else if (line.contains("Address")) {
                    stringBuilder.append(line).append(", ").append(this.data.get("Address")).append('\n');
                } else {
                    stringBuilder.append(line).append(", ").append(this.data.get("Confidence")).append('\n');
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Can't find file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "idk", Toast.LENGTH_SHORT).show();
        } finally {
            String contents = stringBuilder.toString();
        }
        // I just realized I could just write the file once at the end...
    }

}
