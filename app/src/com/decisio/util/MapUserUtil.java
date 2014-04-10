package com.decisio.util;

import java.util.List;

import com.decisio.R;
import com.decisio.models.CafeMood;
import com.decisio.models.LocationPoint;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class MapUserUtil {

    private final int TOP_FIVE_RESULTS = 5;

    public MapUserUtil(){
        
    }
    
    /*
     * Retrieve all locations in "Location" table
     */

    public void populateMap(){
        ParseQuery<LocationPoint> locQuery = ParseQuery.getQuery(LocationPoint.class);
        locQuery.findInBackground(new FindCallback<LocationPoint>() {

            @Override
            public void done(List<LocationPoint> locQueryResult, ParseException excep) {
                if (locQueryResult != null && excep == null) {
                    if (locQueryResult.size()>0) {
                        for (LocationPoint loc: locQueryResult) {
                            getMoodAtLocation(loc);
                        }
                    }
                    else {
                        // do nothing. TODO: discuss if you want to show any message. Might not be good to show user that there are no entries.
                    }
                } else {
                    excep.printStackTrace();
                }
            }
        });
    }

    //TODO: get corresponding last 5 entries (overallMood) from CafeMood This will be icon 
    // Take average (1 for sad, 2 for neutral, 3 for Happy) and display respective face (1 to 1.5 is sad), (1.5 to 2.25 is neutral) (2.25 to 3 is Happy)
    // For each location, from last 5 entries, select that ques that has max "true" considering it was last updated recently. This will be shown as snippet.

    private void getMoodAtLocation(final LocationPoint loc){
        ParseQuery<CafeMood> moodQuery = ParseQuery.getQuery(CafeMood.class);
        moodQuery.whereEqualTo("Id", loc.getInt("Id"));
        moodQuery.setLimit(TOP_FIVE_RESULTS);
        moodQuery.findInBackground(new FindCallback<CafeMood>() {

            @Override
            public void done(List<CafeMood> moodQueryResult, ParseException excep) {
                float averageMood = 0;

                if (moodQueryResult != null && excep == null) {
                    if (moodQueryResult.size()>0) {
                        for (CafeMood mood: moodQueryResult){
                            averageMood += mood.getInt("overallMood");
                        }
                        averageMood = (averageMood/moodQueryResult.size());

                        String locName = loc.getString("Name");
                        if (averageMood >1 && averageMood < 1.5) {
                            // SAD
                            MapUtil.addMarkerFromDB(R.drawable.ic_sad_face, loc, locName, "");
                        } else if (averageMood >= 1.5 && averageMood < 2.25) {
                            // NEUTRAL
                            MapUtil.addMarkerFromDB(R.drawable.ic_neutral_face, loc, locName, "");
                        } else {
                            // HAPPY
                            MapUtil.addMarkerFromDB(R.drawable.ic_happy_face, loc, locName, "");
                        }
                    }
                } else {
                    excep.printStackTrace();
                }
            }
        });
    }

    /*
     * if (rating == 1.0) 
                    overallMood = "SAD";
                else if (rating == 2.0) 
                    overallMood = "NEUTRAL";
                else if (rating == 3.0)
                    overallMood = "HAPPY";
     */
}
