package com.decisio.models;

import java.io.Serializable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Location")
public class LocationPoint extends ParseObject implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private String locLatitude;
    private String locLongitude;
    private String locName;
    private String category;
    private int locId;
    private int passcode;
    
    public int getPasscode() {
        return passcode;
    }

    public LocationPoint () {
        
    }
    
    public LocationPoint(int id, String category, String locLat, String locLong, String locName, int pass) {
        this.locId = id;
        this.category = category;
        this.locLatitude = locLat;
        this.locLongitude = locLong;
        this.locName = locName;
        this.passcode = pass;
    }

    public String getLocLatitude() {
        return locLatitude;
    }

    public String getLocLongitude() {
        return locLongitude;
    }

    public String getLocName() {
        return locName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }
}
