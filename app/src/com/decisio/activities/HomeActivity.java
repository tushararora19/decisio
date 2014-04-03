package com.decisio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.decisio.R;
import com.decisio.fragments.LoginFragment;
import com.decisio.models.LocationPoint;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class HomeActivity extends FragmentActivity implements LoginFragment.NotifyActivityListener{

    private final String TAG_LOGIN_FRAG = "Manager Login Fragment"; 
    private TextView tvNewSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvNewSignup = (TextView) findViewById(R.id.tv_new_location_signup);
        onClickNewLocationEnter();

        /* 
        LocationPoint loc = new LocationPoint();
        loc.put("Id", 3);
        loc.put("Category", "Cafe");
        loc.put("Passcode", 1234);

        loc.saveInBackground(); */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public void onClickOpenMap (View v) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra("source", "user");
        startActivity(intent);
    }

    public void onClickOpenManagerConsole(View v) {
        showLogin();       
    }

    private void onClickNewLocationEnter () {
        // TODO: input new location. 
        // Fragment1 : Allow manager to enter location name (full address) . This can be achieved by displaying map to manager with search field.
        // Fragment2 :Click Next -> Enter passcode which we will generate manually once we have deal with that cafe. and click submit / finish.
        tvNewSignup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                intent.putExtra("source", "manager");
                startActivity(intent);                
            }
        });
    }

    private void showLogin () {
        LoginFragment loginFrag = new LoginFragment();
        loginFrag.setListener(HomeActivity.this);
        loginFrag.show(getSupportFragmentManager(), TAG_LOGIN_FRAG);
    }

    @Override
    public void onSignIn(DialogFragment dialogFragment, String locId, String passwd) {
        // TODO: start intent only after successful login
        authenticateLogin(locId, passwd);
    }

    @Override
    public void onCancelSignIn(DialogFragment dialogFragment) {
        Toast.makeText(getApplicationContext(), "Cancel Log in", Toast.LENGTH_SHORT).show();

    }

    private void authenticateLogin(final String user, String pwd) {
        ParseQuery<LocationPoint> query = ParseQuery.getQuery(LocationPoint.class);
        query.whereEqualTo("Id", Integer.parseInt(user));
        query.whereEqualTo("Passcode", Integer.parseInt(pwd));

        query.getFirstInBackground(new GetCallback<LocationPoint>() {

            @Override
            public void done(LocationPoint queryResults, ParseException excep) {
                if (excep==null){
                    if (queryResults!=null) {
                        Toast.makeText(getApplicationContext(), "Log in successful.", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), ManagerConsole.class);
                        intent.putExtra("locId", user);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Invalid User / Password.", Toast.LENGTH_SHORT).show();
                        showLogin();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid User / Password.", Toast.LENGTH_SHORT).show();
                    showLogin();
                }
            }
        });
    }
}
