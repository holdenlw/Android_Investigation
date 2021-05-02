package com.example.myapplication;


import java.util.ArrayList;
import java.util.HashMap;

public class StoreInfo {

    private HashMap<String, ArrayList<String>> data;
    private String id;

    public StoreInfo(String id) {
        // id = Android Device ID + YYYY/MM
        this.id = id;
        // if txt file already exists
        // data[keys] = txt[keys]
        // else
        // data = {
        //  key: []
        //  create new txt file with ID as name with data as the content
    }

    // setters for each key of data

    // option: either append to file each setter call, or append all changes at one time

}
