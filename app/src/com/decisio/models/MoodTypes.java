package com.decisio.models;

import com.decisio.R;


public enum MoodTypes {

    HAPPY("happy", R.drawable.ic_happy_face),
    SAD("sad", R.drawable.ic_sad_face),
    NEUTRAL("neutral", R.drawable.ic_neutral_face);
    
    private final String moodName;
    private final int imgResId;

    private MoodTypes(String moodName, int imgResId) {
        this.moodName = moodName;
        this.imgResId = imgResId;
    }

    public String getMoodName() {
        return moodName;
    }

    public int getImgResId() {
        return imgResId;
    }
    
    
}
