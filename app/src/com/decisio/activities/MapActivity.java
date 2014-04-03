package com.decisio.activities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.decisio.R;
import com.decisio.models.CafeMood;
import com.decisio.models.LocationPoint;
import com.decisio.util.MapUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class MapActivity extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationClient mLocationClient;
    private SearchView svLocationSearch;
    private Geocoder geoCoder;
    private final int MAX_RESULTS = 1; 
    private final String TAG = "MapActivity";
    private Marker marker;
    private LocationPoint managerLoc;

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        svLocationSearch = (SearchView) findViewById(R.id.sv_search_location);
        geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        mLocationClient = new LocationClient(this, this, this);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            map = mapFragment.getMap();
            if (map != null) {
                Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
                map.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        setSearchListener();

        // distinguish if the request came from user or manager signing up first time.
        setSelectLocationListener();
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        if (isGooglePlayServicesAvailable()) {
            mLocationClient.connect();
        }

    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

        case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try to connect again
             */
            switch (resultCode) {
            case Activity.RESULT_OK:
                mLocationClient.connect();
                break;
            }

        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Location location = mLocationClient.getLastLocation();
        if (location != null) {
            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);

            // TODO: get locations from backend table "Location.
            // for each location id, get its name: title of marker when clicked. 
            // get corresponding last 5 entries (overallMood) from CafeMood This will be icon 
            // Take average (1 for sad, 2 for neutral, 3 for Happy) and display respective face (1 to 1.5 is sad), (1.5 to 2.25 is neutral) (2.25 to 3 is Happy)
            // For each location, from last 5 entries, select that ques that has max "true" considering it was last updated recently. This will be shown as snippet.

            populateMap();
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    public void clickOpenManagerConsole(View v) {
        Intent intent = new Intent(this, ManagerConsole.class);
        startActivity(intent);
    }

    private void setSearchListener(){
        svLocationSearch.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String location) {
                MapUtil.hideSoftKeyboard(svLocationSearch, MapActivity.this);
                try {
                    if (marker!=null) 
                        marker.remove();
                    LocationPoint loc = getAddress(location);

                    if (getIntent().getStringExtra("source").equals("manager")){
                        managerLoc = loc;
                    } 

                    if (loc != null) {
                        //addMarker(R.drawable.ic_happy_face, loc, loc.getLocName(), "Random reason");
                        addMarker(R.drawable.ic_marker_pin, loc, loc.getLocName(), "");
                        LatLng latLng = new LatLng(loc.getLocLatitude(), loc.getLocLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                        map.animateCamera(cameraUpdate);
                        // clearing search field
                        svLocationSearch.setQuery("", false);
                    } 
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException ioe) {
                    ioe.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String location) {
                MapUtil.showSoftKeyboard(svLocationSearch, MapActivity.this);
                return false;
            }
        });
    }

    private void setSelectLocationListener () {
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (getIntent().getStringExtra("source").equalsIgnoreCase("user")){
                    Intent intent = new Intent(getApplicationContext(), UserResponseActivity.class);
                    // get user Loc id (this user can be any location which is clicked). Hence, there needs to be a method to get loc id from Marker !
                    Log.d(TAG, marker.getTitle().substring(0, marker.getTitle().indexOf(":")).trim());
                    intent.putExtra("LocationId", Integer.parseInt(marker.getTitle().substring(0, marker.getTitle().indexOf(":")).trim()));
                    startActivity(intent);
                } else {
                    showConfirmSelectionPopup();    
                }
            }
        });    

    }

    private void showConfirmSelectionPopup () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(managerLoc.getLocName())     //show loc name here
        .setMessage("Are you sure you want to make this your location ?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // generate Loc if / passcode etc and save in db
                showChooseLocationIdPopUp();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "Cancel selection NO !!");

                if (!svLocationSearch.getQuery().toString().isEmpty())
                    svLocationSearch.setQuery("", false);
            }
        });
        builder.show();
    }

    private void showChooseLocationIdPopUp(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Choose location ID");

        // Set an EditText view to get user input 
        final EditText etLocId = new EditText(this);
        etLocId.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(etLocId);

        alert.setPositiveButton("Sign-Up", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String locId = etLocId.getText().toString();

                // check if this loc id exists ? if not, confirm sign up, put it to table and show user that he'll receive pwd email for sign up confirmation soon.

                ParseQuery<LocationPoint> query = ParseQuery.getQuery(LocationPoint.class);
                query.whereEqualTo("Id", Integer.parseInt(locId));

                query.findInBackground(new FindCallback<LocationPoint>() {

                    @Override
                    public void done(List<LocationPoint> queryResult, ParseException excep) {
                        if (queryResult != null) {
                            if (queryResult.size()>0) {
                                Toast.makeText(getApplicationContext(), "Location Id taken. Choose new one.", Toast.LENGTH_SHORT).show();
                                showChooseLocationIdPopUp(); 
                            }
                            else {
                                // location id is unique. take it and store it in db. Send email to us to approve.
                                managerLoc.put("Id", Integer.parseInt(locId));
                                managerLoc.put("Category", "Cafe");
                                managerLoc.put("Name", managerLoc.getLocName());
                                managerLoc.put("latitude", Double.toString(managerLoc.getLocLatitude()));
                                managerLoc.put("longitude", Double.toString(managerLoc.getLocLongitude()));

                                managerLoc.saveInBackground();

                                Toast.makeText(getApplicationContext(), "Location Id saved. Please check your email in sometime for pass code." +locId, Toast.LENGTH_SHORT).show();
                                // send email now.
                                // take him back to home screen.
                                finish();
                            }
                        }
                    }
                });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    private LocationPoint getAddress(String location) throws IOException, IndexOutOfBoundsException{
        List<Address> address = geoCoder.getFromLocationName(location, MAX_RESULTS);
        LocationPoint loc = null;
        if (address.size()>0) {
            Address add = address.get(0);

            StringBuilder fullAddName =  new StringBuilder();
            for (int i=0;i< add.getMaxAddressLineIndex();i++)
                fullAddName.append(add.getAddressLine(i)+",");

            loc = new LocationPoint(
                    address.get(0).getLatitude(),
                    address.get(0).getLongitude(), 
                    fullAddName.toString().substring(0, fullAddName.toString().length()-1));
        } else {
            Toast.makeText(getApplicationContext(), "Search failed to find any such location", Toast.LENGTH_SHORT).show();
        }

        return loc;

    }

    private void addMarker(int resourceId, LocationPoint loc, String title, String snippet){
        marker = map.addMarker(new MarkerOptions()
        .position(new LatLng(loc.getLocLatitude(), loc.getLocLongitude()))
        .title(title)
        .snippet(snippet)
        .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    private void addMarkerFromDB (int resourceId, LocationPoint loc, String title, String snippet){
        map.addMarker(new MarkerOptions()
        .position(new LatLng(Double.parseDouble(loc.getString("latitude")), Double.parseDouble(loc.getString("longitude"))))
        .title(loc.getInt("Id") + " :"+ title)
        .snippet(snippet)
        .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    private void populateMap(){
        ParseQuery<LocationPoint> locQuery = ParseQuery.getQuery(LocationPoint.class);


        locQuery.findInBackground(new FindCallback<LocationPoint>() {

            @Override
            public void done(List<LocationPoint> locQueryResult, ParseException excep) {
                if (locQueryResult != null) {
                    if (locQueryResult.size()>0) {
                        for (LocationPoint loc: locQueryResult) {
                            getMoodAtLocation(loc);
                        }
                    }
                    else {
                        // no location found
                    }
                }
            }
        });
    }

    private void getMoodAtLocation(final LocationPoint loc){
        ParseQuery<CafeMood> moodQuery = ParseQuery.getQuery(CafeMood.class);
        moodQuery.whereEqualTo("Id", loc.getInt("Id"));
        moodQuery.findInBackground(new FindCallback<CafeMood>() {

            @Override
            public void done(List<CafeMood> moodQueryResult, ParseException excep) {
                float averageMood = 0;

                if (moodQueryResult != null) {
                    if (moodQueryResult.size()>0) {
                        for (CafeMood mood: moodQueryResult){
                            averageMood += mood.getInt("overallMood");
                        }
                        averageMood = (averageMood/moodQueryResult.size());

                        if (averageMood >1 && averageMood < 1.5) {
                            // SAD
                            addMarkerFromDB(R.drawable.ic_sad_face, loc, loc.getString("Name"), "");
                        } else if (averageMood >= 1.5 && averageMood < 2.25) {
                            // nNEUTRAL
                            addMarkerFromDB(R.drawable.ic_neutral_face, loc, loc.getString("Name"), "");
                        } else {
                            // HAPPY
                            addMarkerFromDB(R.drawable.ic_happy_face, loc, loc.getString("Name"), "");
                        }
                    }
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