package com.decisio.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("CafeMood")
public class CafeMood extends ParseObject{

    private int locId; // corresponds to id in Location
    private int overallMood;
    private boolean ques1Selected;
    private boolean ques2Selected;
    private boolean ques3Selected;
    
    public CafeMood(){
        ques1Selected = false;
        ques2Selected = false;
        ques3Selected = false;
        overallMood = 2;
    }

    public int getOverallMood() {
        return overallMood;
    }

    public void setOverallMood(int overallMood) {
        this.overallMood = overallMood;
    }

    public boolean isQues1Selected() {
        return ques1Selected;
    }

    public void setQues1Selected(boolean ques1Selected) {
        this.ques1Selected = ques1Selected;
    }

    public boolean isQues2Selected() {
        return ques2Selected;
    }

    public void setQues2Selected(boolean ques2Selected) {
        this.ques2Selected = ques2Selected;
    }

    public boolean isQues3Selected() {
        return ques3Selected;
    }

    public void setQues3Selected(boolean ques3Selected) {
        this.ques3Selected = ques3Selected;
    }
    
    public int getLocId() {
        return locId;
    }

    public void setLocId(int locId) {
        this.locId = locId;
    }

    
}
