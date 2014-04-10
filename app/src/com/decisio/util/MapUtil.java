package com.decisio.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.decisio.DecisioApp;
import com.decisio.R;
import com.decisio.activities.MapActivity;
import com.decisio.models.LocationPoint;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class MapUtil {

    private final static int MAX_RESULTS = 1; 
    private static Geocoder geoCoder = new Geocoder(DecisioApp.getContext(), Locale.getDefault());
    private static int UNIQUE_LOC_ID = -1;
    private static LocationPoint loc = null;
    private final static int OTHER_LOC_ZOOM_VALUE = 16;

    public static LocationPoint getLoc() {
        return loc;
    }

    public static void showSoftKeyboard(View v, Activity activity) { 
        if (v.requestFocus()){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideSoftKeyboard(View v, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static Marker addMarker(int resourceId, LocationPoint loc, String title, String snippet){
        return MapActivity.getMap().addMarker(new MarkerOptions()
        .position(new LatLng(Double.parseDouble(loc.getLocLatitude()), Double.parseDouble(loc.getLocLongitude())))
        .title(title)
        .snippet(snippet)
        .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    public static void addMarkerFromDB (int resourceId, LocationPoint loc, String title, String snippet){
        MapActivity.getMap().addMarker(new MarkerOptions()
        .position(new LatLng(Double.parseDouble(loc.getString("latitude")), Double.parseDouble(loc.getString("longitude"))))
        .title(loc.getInt("Id") + " :"+ title)
        .snippet(snippet)
        .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    public static void saveLocationInBackend(LocationPoint loc, int locId, String category, String name, String latitude, String longitude, int passcode) {
        loc.put("Id", locId);
        loc.put("Category", category);
        loc.put("Name", name);
        loc.put("latitude", latitude);
        loc.put("longitude", longitude);
        loc.put("Passcode", passcode);

        loc.saveInBackground();
    }

    public static void getLocationFromName(String location) throws IOException, IndexOutOfBoundsException{
        List<Address> address = geoCoder.getFromLocationName(location, MAX_RESULTS);
        if (address.size()>0) {
            Address add = address.get(0);
            // search for this location in db (match lat / long) 
            searchLocationInDB(add);
        } else {
            Toast.makeText(DecisioApp.getContext(), "Search failed to find any such location", Toast.LENGTH_SHORT).show();
        }
    }

    public static void searchLocationInDB (final Address add) {
        ParseQuery<LocationPoint> query = ParseQuery.getQuery(LocationPoint.class);
        query.whereEqualTo("latitude", Double.toString(add.getLatitude()));
        query.whereEqualTo("longitude", Double.toString(add.getLongitude()));

        query.getFirstInBackground(new GetCallback<LocationPoint>() {
            @Override
            public void done(LocationPoint queryResult, ParseException excep) {
                UNIQUE_LOC_ID = -1;
                if (queryResult!=null && excep==null) {
                    // meaning there's a match
                    Toast.makeText(DecisioApp.getContext(), "Location Id already exists", Toast.LENGTH_SHORT).show();
                    loc = new LocationPoint(queryResult.getInt("Id"), queryResult.getString("Category"), queryResult.getString("latitude"), queryResult.getString("longitude"), queryResult.getString("Name"), queryResult.getInt("Passcode"));
                    pointOnMap(false);
                } else {
                    generateNewLocId (add);
                }
            }
        });
    }

    public static void generateNewLocId(final Address add) {
        ParseQuery<LocationPoint> query = ParseQuery.getQuery(LocationPoint.class);
        query.addDescendingOrder("Id");

        query.getFirstInBackground(new GetCallback<LocationPoint>() {

            @Override
            public void done(LocationPoint queryResult, ParseException excep) {

                StringBuilder fullAddName =  new StringBuilder();
                for (int i=0;i< add.getMaxAddressLineIndex();i++)
                    fullAddName.append(add.getAddressLine(i)+",");

                if (queryResult!=null && excep == null) { 
                    UNIQUE_LOC_ID = queryResult.getInt("Id") +1;
                } else {
                    // first entry to db
                    UNIQUE_LOC_ID = 1;
                }
                loc = new LocationPoint(
                        UNIQUE_LOC_ID,
                        "Cafe",
                        Double.toString(add.getLatitude()),
                        Double.toString(add.getLongitude()), 
                        fullAddName.toString().substring(0, fullAddName.toString().length()-1),
                        1903);

                pointOnMap(true);
            }
        });
    }

    private static void pointOnMap (boolean showMarker){ 
        if (loc != null) {
            if (showMarker)
                MapUtil.addMarker(R.drawable.ic_marker_pin, loc, loc.getLocName(), "");
            else {
                if (MapActivity.getMarker()!=null)
                    MapActivity.getMarker().remove();
            }
            LatLng latLng = new LatLng(Double.parseDouble(loc.getLocLatitude()), Double.parseDouble(loc.getLocLongitude()));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, OTHER_LOC_ZOOM_VALUE);
            MapActivity.getMap().animateCamera(cameraUpdate);
        }
    }
}
