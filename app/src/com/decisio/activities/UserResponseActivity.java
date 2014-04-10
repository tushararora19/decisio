package com.decisio.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.decisio.R;
import com.decisio.adapters.QuesAdapter;
import com.decisio.models.CafeMood;
import com.decisio.models.LocationPoint;
import com.decisio.models.ManagerQuestions;
import com.decisio.util.MapUtil;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class UserResponseActivity extends Activity implements OnItemClickListener{

    private static final String TAG = "User Response";
    private RatingBar rBar;
    private ListView lvQues;
    private EditText etOtherComments;
    private TextView tvCharsLeft;
    private final int MAX_COUNT = 140;
    private final Set<String> selectedParameters = new HashSet<String>();
    private List<String> questionParameters;
    private QuesAdapter quesAdap;
    private LocationPoint userSelectedloc;
    private int userSelectedLocId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_response);
        rBar = (RatingBar) findViewById(R.id.rb_overall);
        lvQues = (ListView) findViewById(R.id.lv_ques_parameters);
        etOtherComments = (EditText) findViewById(R.id.et_other);
        tvCharsLeft = (TextView) findViewById(R.id.tv_char_left);
        questionParameters = new ArrayList<String>();

        userSelectedloc = (LocationPoint) getIntent().getSerializableExtra("userSelectedLocation");
        userSelectedLocId = getIntent().getIntExtra("LocationId", -1); 
        selectedParameters.clear();
        quesAdap = new QuesAdapter(getApplicationContext(), questionParameters);
        lvQues.setAdapter(quesAdap);

        lvQues.setOnItemClickListener(this);

        setCharLeftListener();
        setRatingBarListener();

        clearExistingQuestions();
        populateQuestionsForLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_response, menu);
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

    /*
     * LISTENERS
     */

    public void onClickSwitchToMap(MenuItem mi) {
        // Prompt user that he's saving it.
        saveUserResponse();
        setResult(this.RESULT_OK);
        finish();
    }

    private void setRatingBarListener(){

        rBar.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float touchPositionX = event.getX();
                    float width = rBar.getWidth();
                    float starsf = (touchPositionX / width) * 3.0f;
                    int stars = (int)starsf + 1;
                    rBar.setRating(stars);

                    v.setPressed(false);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setPressed(false);
                }

                return true;
            }
        });
    }

    //    private void setLocIdDoneListener(){
    //        etLocId.setOnKeyListener(new OnKeyListener() {
    //
    //            @Override
    //            public boolean onKey(View v, int keyCode, KeyEvent event) {
    //                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    //                    Toast.makeText(getApplicationContext(), "Location Entered", Toast.LENGTH_SHORT).show();
    //                    locId = Integer.parseInt(etLocId.getText().toString().trim());
    //                    // TODO: check if this locId exists (else delete entry and re-prompt)
    //                    clearExistingQuestions();
    //                    // get Questions from backend and populate questions list
    //                    populateQuestionsForLocation();
    //                }       
    //                return false;
    //            }
    //        });
    //    }

    private void setCharLeftListener(){
        etOtherComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                Log.d(TAG, arg0.toString());
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                //mi_charsLeft = (MenuItem) findViewById(R.id.mi_charsLeft);
                int left = MAX_COUNT - etOtherComments.getText().toString().length();
                if (left >=0 )
                    tvCharsLeft.setText(left+" chars");
                else 
                    Toast.makeText(getApplicationContext(), "Max 140 chars allowed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

        CheckedTextView nameView = (CheckedTextView) view.findViewById(R.id.ctv_ques);
        String ques = nameView.getText().toString(); 

        if (selectedParameters.contains(ques)) {
            selectedParameters.remove(ques);
        } else {
            selectedParameters.add(ques);
        }

        nameView.setChecked(selectedParameters.contains(ques));
    }

    /*
     * Backend Methods (to retrieve data from db)
     */

    private void populateQuestionsForLocation(){
        ParseQuery<ManagerQuestions> query = ParseQuery.getQuery(ManagerQuestions.class);
        if (userSelectedloc!=null)
            query.whereEqualTo("Id", userSelectedloc.getInt("Id"));
        else 
            query.whereEqualTo("Id", userSelectedLocId);

        query.getFirstInBackground(new GetCallback<ManagerQuestions>() {

            @Override
            public void done(ManagerQuestions queryResult, ParseException excep) {
                if (queryResult != null) {
                    if (excep == null ) {
                        questionParameters.add(queryResult.getString("Ques1"));
                        questionParameters.add(queryResult.getString("Ques2"));
                        questionParameters.add(queryResult.getString("Ques3"));

                        quesAdap.notifyDataSetChanged();

                        Toast.makeText(getApplicationContext(), "Ques Parameters populated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Error retrieving existing Questions. "+excep.getMessage());
                    }     
                } else {
                    Log.d("TAG", "No data Found. ");
                    // TODO: populate with pre-defined questions.
                    questionParameters.add("Predefined Ques1");
                    questionParameters.add("Predefined Ques2");
                    questionParameters.add("Predefined Ques3");

                    quesAdap.notifyDataSetChanged();
                }
            }
        });
    }

    private void saveUserResponse(){
        CafeMood mood = new CafeMood();

        if (!questionParameters.isEmpty()) {
            mood.setQues1Selected(selectedParameters.contains(questionParameters.get(0)));
            mood.setQues2Selected(selectedParameters.contains(questionParameters.get(1)));
            mood.setQues3Selected(selectedParameters.contains(questionParameters.get(2)));
        }
        int locId = -1;
        if (userSelectedloc != null)
            locId = userSelectedloc.getLocId();
        else 
            locId = userSelectedLocId;

        mood.setLocId(locId);
        mood.setOverallMood((int)rBar.getRating());
        if (mood.getOverallMood()!=0) { 
            mood.put("Ques1", mood.isQues1Selected());
            mood.put("Ques2", mood.isQues2Selected());
            mood.put("Ques3", mood.isQues3Selected());
            mood.put("overallMood", mood.getOverallMood());

            if (locId != -1) {
                mood.put("Id", locId);
                if (userSelectedLocId == -1){
                    LocationPoint loc = new LocationPoint();
                    MapUtil.saveLocationInBackend(userSelectedloc, locId, "Cafe", userSelectedloc.getLocName(), userSelectedloc.getLocLatitude(), userSelectedloc.getLocLongitude(), userSelectedloc.getPasscode());
                    // TODO: show smiley on map now corresponding to this location 
                }
                mood.saveInBackground();
            }
            else {
                // there is nothing to do or save in backend
            }
        }
    }

    private void clearExistingQuestions () {
        questionParameters.clear();
        quesAdap.notifyDataSetChanged();
    }

}
