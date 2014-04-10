package com.decisio.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.decisio.R;
import com.decisio.models.ManagerQuestions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class ManagerConsole extends Activity {

    private EditText etQues1;
    private EditText etQues2;
    private EditText etQues3;
    private static final String TAG = "Manager Console";
    private List<String> questions;
    private int locId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_console);

        etQues1 = (EditText) findViewById(R.id.et_ques1);
        etQues2 = (EditText) findViewById(R.id.et_ques2);
        etQues3 = (EditText) findViewById(R.id.et_ques3);

        questions = new ArrayList<String>();

        try {
            locId = Integer.parseInt(getIntent().getStringExtra("locId"));
        } catch (NullPointerException npe) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manager_console, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // check if this does desired behavior
        super.onBackPressed();
    }

    public void clickToCancel(View v) {
        finish();
    }

    public void clickToSubmit(View v) {
        saveQuestions();
        finish();
    }

    private void saveQuestions(){
        try {
            String ques = etQues1.getText().toString().trim();
            if (!ques.isEmpty())
                questions.add(ques);
            ques = etQues2.getText().toString().trim();
            if (!ques.isEmpty())
                questions.add(ques);
            ques = etQues3.getText().toString().trim();
            if (!ques.isEmpty())
                questions.add(ques);

            ManagerQuestions newQuestions = new ManagerQuestions(locId, questions);

            fetchAndRemoveCurrentQuestions(locId, newQuestions);

        } catch (NullPointerException npe){
            npe.printStackTrace();
        }

    }

    private void fetchAndRemoveCurrentQuestions(int locId, final ManagerQuestions newQuestions) {
        ParseQuery<ManagerQuestions> query = ParseQuery.getQuery(ManagerQuestions.class);
        query.whereEqualTo("Id", locId);

        query.getFirstInBackground(new GetCallback<ManagerQuestions>() {

            @Override
            public void done(ManagerQuestions queryResult, ParseException excep) {
                if (queryResult != null) {
                    if (excep == null ) {
                        // if id retrieved match then update current entry else make new entry and save in bkend
                        if (Integer.parseInt(queryResult.get("Id").toString().trim()) == newQuestions.getLocId()) {
                            queryResult.setLocId(newQuestions.getLocId());
                            queryResult.setQues1(newQuestions.getQues1());
                            queryResult.setQues2(newQuestions.getQues2());
                            queryResult.setQues3(newQuestions.getQues3());
                            saveInBackend(queryResult);
                            Toast.makeText(getApplicationContext(), "Entries updated successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else 
                            saveInBackend(newQuestions);
                    } else {
                        Log.d(TAG, "Error retrieving existing Questions. "+excep.getMessage());
                        Toast.makeText(getApplicationContext(), "Saving entries Failed !! Try again.", Toast.LENGTH_SHORT).show();
                    }     
                } else {
                    Log.d(TAG, "First entry for this location");
                    saveInBackend(newQuestions);
                    Toast.makeText(getApplicationContext(), "Entries saved successfully.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveInBackend(ManagerQuestions newQuestions) {
        try {
            newQuestions.put("Id", newQuestions.getLocId());
            newQuestions.put("Ques1", newQuestions.getQues1());
            newQuestions.put("Ques2", newQuestions.getQues2());
            newQuestions.put("Ques3", newQuestions.getQues3());

            newQuestions.saveInBackground();
        } catch (IllegalArgumentException iae){ }
    }
}
