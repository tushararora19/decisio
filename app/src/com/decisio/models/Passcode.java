package com.decisio.models;

import android.text.format.Time;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Passcode")
public class Passcode extends ParseObject {

    private int value;
    private Time genTime;
    private Time expTime;
    private int locId;      // this needs to correspond to id in Location;
    
    public Passcode (){ 
        
    }

    public int getValue() {
        return value;
    }

    public Time getGenTime() {
        return genTime;
    }

    public Time getExpTime() {
        return expTime;
    }
    
    private void generatePassCode() {
        
    }
    
}
