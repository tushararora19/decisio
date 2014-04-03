package com.decisio.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Location")
public class LocationPoint extends ParseObject{

    private double locLatitude;
    private double locLongitude;
    private String locName;
    private String category;
    private int locId;
    private String passcode;
    
    public LocationPoint () {
        
    }
    
    public LocationPoint(double locLat, double locLong, String locName) {
        this.locLatitude = locLat;
        this.locLongitude = locLong;
        this.locName = locName;
    }

    public double getLocLatitude() {
        return locLatitude;
    }

    public double getLocLongitude() {
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
