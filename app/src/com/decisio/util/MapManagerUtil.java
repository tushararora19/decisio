package com.decisio.util;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.decisio.DecisioApp;
import com.decisio.activities.MapActivity;
import com.decisio.models.LocationPoint;

public class MapManagerUtil {

    private MapActivity mapAct;
    private LocationPoint managerLoc;
    private final String TAG = "MapManagerUtil";

    public MapManagerUtil(MapActivity mapAct, LocationPoint loc) {
        this.mapAct = mapAct;
        this.managerLoc = loc;
    }

    // THIS IS MANAGER SPECIFIC
    public void showConfirmSelectionPopup (final SearchView svLocationSearch) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mapAct);
        builder.setTitle(managerLoc.getLocName())     //show loc name here
        .setMessage("Are you sure you want to make this your location ?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // generate Loc if / passcode etc and save in db
                showChooseLocationIdPopUp(managerLoc.getLocName());
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

    // THIS IS MANAGER SPECIFIC
    public void showChooseLocationIdPopUp(String locName){
        AlertDialog.Builder alert = new AlertDialog.Builder(mapAct);

        alert.setTitle("Your Location ID");

        // Set an EditText view to get user input 
        final TextView tvLocId = new TextView(mapAct);

        try {
            MapUtil.getLocationFromName(locName);
            
            final LocationPoint loc = MapUtil.getLoc();
            if (loc!=null) {
                tvLocId.setText(loc.getLocId()+"");
                tvLocId.setGravity(Gravity.CENTER_HORIZONTAL);
                alert.setView(tvLocId);
                alert.setPositiveButton("Finish Sign-Up", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MapUtil.saveLocationInBackend(loc, loc.getInt("Id"), "Cafe", loc.getLocName(), loc.getLocLatitude(), loc.getLocLongitude(), 1906);
                        Toast.makeText(mapAct.getApplicationContext(), "Location Id saved. Please check your email in sometime for pass code." +loc.getInt("Id"), Toast.LENGTH_SHORT).show();
                        // TODO: send us email now and then him saying we'll get back.
                        //((Activity) ctx).finish();
                        mapAct.finish();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();
            }
            else 
                Toast.makeText(DecisioApp.getContext(), "Location exists", Toast.LENGTH_SHORT);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
