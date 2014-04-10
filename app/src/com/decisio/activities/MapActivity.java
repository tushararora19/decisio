package com.decisio.activities;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.decisio.R;
import com.decisio.models.LocationPoint;
import com.decisio.util.MapManagerUtil;
import com.decisio.util.MapUserUtil;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MapActivity extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

    // TODO: remove marker from location searched by user when he comes back from response screen (but display that point i.e. he should see that point instead of his current location). Do this in onResume

    private SupportMapFragment mapFragment;
    public static GoogleMap map;
    private LocationClient mLocationClient;
    private SearchView svLocationSearch;
    private final String TAG = "MapActivity";
    private static Marker marker;
    private LocationPoint managerLoc, userLoc;
    private MapUserUtil mapUser;
    private MapManagerUtil mapManager;
    private final int SEARCH_REQ_CODE = 1000;
    private final int USER_CURRENT_LOC_ZOOM_VALUE = 15;

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

        mapUser = new MapUserUtil();
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

        case SEARCH_REQ_CODE:
            if (resultCode == Activity.RESULT_OK) {
                if (marker!=null) 
                    marker.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, USER_CURRENT_LOC_ZOOM_VALUE);
            map.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }

        if (marker!=null)
            marker.remove();
        mapUser.populateMap();        
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
                    MapUtil.getLocationFromName(location);
                    svLocationSearch.setQuery("", false);
                    // TODO: this is a temporary solution to remove focus and have back click working in first attempt.
                    // However, this will work only if something is searched, in case nothing is searched (and user had clicked searchView), he'll have to click back twice.
                    svLocationSearch.clearFocus();
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
                // distinguish if user is manager / general user
                if (getIntent().getStringExtra("source").equalsIgnoreCase("user")){
                    userLoc = MapUtil.getLoc();
                    Intent intent = new Intent(getApplicationContext(), UserResponseActivity.class);
                    // TODO: there is no id for a new location that user searches and that is not in our db
                    try {
                        intent.putExtra("LocationId", Integer.parseInt(marker.getTitle().substring(0, marker.getTitle().indexOf(":")).trim()));
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } 
                    try {
                        intent.putExtra("userSelectedLocation", userLoc);
                    } catch (NullPointerException npe) {
                        npe.printStackTrace(); // when we don't search
                    }
                    if (marker!=null) {
                        marker.remove();
                    }
                    startActivityForResult(intent, SEARCH_REQ_CODE);
                } else {
                    managerLoc = MapUtil.getLoc();
                    mapManager = new MapManagerUtil(MapActivity.this, managerLoc);
                    mapManager.showConfirmSelectionPopup(svLocationSearch);
                }
            }
        });    
    }

    public static Marker getMarker() {
        return marker;
    }

}