package org.bitbucket.kptsui.bps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final static String CAR_PARK_ID = "f1tMOZIDmZ";

    private TextView availableSpaces;
    private TextView totalSpaces;
    private TextView carParkName;
    private TextView carParkAddress;

    private ResideMenu resideMenu;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemRecord;
    private ResideMenuItem itemShare;
    private ResideMenuItem itemSettings;

    private ActionBar actionBar;

    private RequestQueue requestQueue;

    private static int hourlyRate = 40;

    public static int getHourlyRate() {
        return hourlyRate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu);

        setUpSideMenu();

        availableSpaces = (TextView) findViewById(R.id.availableSpaces);
        totalSpaces = (TextView) findViewById(R.id.totalSpaces);
        carParkName = (TextView) findViewById(R.id.carParkName);
        carParkAddress = (TextView) findViewById(R.id.carParkAddress);

        requestQueue = Volley.newRequestQueue(this);

        updateParkingSpaces();
    }

    private void setUpSideMenu(){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);

        //resideMenu.setScaleValue(0.5f);
        //resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        // create menu items;
        itemProfile  = new ResideMenuItem(this, R.drawable.ic_profile,  "Profile");
        itemRecord  = new ResideMenuItem(this, R.drawable.ic_calendar,  "Record");
        itemShare = new ResideMenuItem(this, R.drawable.ic_share, "Share");
        itemSettings = new ResideMenuItem(this, R.drawable.ic_settings, "Settings");

        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemRecord, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemShare, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);

        itemProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ParseUser.getCurrentUser() != null)
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));

                resideMenu.closeMenu();
            }
        });
        itemRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.closeMenu();
            }
        });
        itemShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.closeMenu();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.closeMenu();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateParkingSpaces(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CarPark");
        query.getInBackground(CAR_PARK_ID, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d(App.TAG, "Fetched object: " + object);

                    MainActivity.this.hourlyRate = object.getInt("hourlyRate");

                    int availableSpaces = object.getInt("availableSpace");
                    MainActivity.this.availableSpaces.setText(String.valueOf(availableSpaces));

                    int totalSpaces = object.getInt("totalAvailableSpace");
                    MainActivity.this.totalSpaces.setText(String.valueOf(totalSpaces));

                    MainActivity.this.carParkName.setText(object.getString("name"));
                    MainActivity.this.carParkAddress.setText(object.getString("address"));
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.toString());
                }
            }
        });
    }

    public void findMyCar(View v){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Finding Your Car Lot");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingSpace");

        query.whereEqualTo("currentUser", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                progressDialog.dismiss();

                if (e == null && object != null) { // user parked before
                    Log.d(App.TAG, "Fetched object: " + object);
                    String parkingLotId = object.getString("parkingLotId");

                    Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                    intent.putExtra("parkingLotId", parkingLotId);
                    intent.putExtra("isDirectedGraph", 0);
                    startActivity(intent);
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.getMessage());
                    Toast.makeText(App.getInstance(), "You did not park a car", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void parkMyCar(View v){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_park_time_select, null);
        final Switch mSwitch = (Switch) view.findViewById(R.id.switchSpecifyParkingTime);
        final NumberPicker mNumberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        mNumberPicker.setVisibility(View.GONE);
        mNumberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                if(i < 2)
                    return i + " hour";
                else
                    return i + " hours";
            }
        });
        mNumberPicker.setMaxValue(24);
        mNumberPicker.setMinValue(1);
        mNumberPicker.setValue(1);
        mNumberPicker.setWrapSelectorWheel(false);
        // disable click to input value manually
        //mNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mNumberPicker.setValue(mNumberPicker.getMinValue());
                    mNumberPicker.setVisibility(View.VISIBLE);
                }
                else {
                    mNumberPicker.setVisibility(View.GONE);
                }

                Log.d(App.TAG, "mNumberPicker.setEnabled(isChecked): " + isChecked);
            }
        });

        dialog.setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Getting Car Lot");
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.show();

                        Log.d(App.TAG, "mSwitch.isChecked(): "+mSwitch.isChecked()+", mNumberPicker.getValue(): " + mNumberPicker.getValue());

                        Map<String, Integer> map = new HashMap<>();

                        if(mSwitch.isChecked()){
                            int selectedTime = mNumberPicker.getValue();
                            if(selectedTime > 0){
                                map.put("time", selectedTime);
                            }
                        }

                        ParseCloud.callFunctionInBackground("checkin", map, new FunctionCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parkingSpace, ParseException e) {
                                progressDialog.dismiss();

                                if (e == null) {
                                    try {
                                        Log.d(App.TAG, parkingSpace.getClassName());
                                        String parkingLotId = parkingSpace.getString("parkingLotId");
                                        Log.d(App.TAG, parkingLotId);
                                        Toast.makeText(App.getInstance(), "Check-in succeeded\nYour Car Lot is: " + parkingLotId, Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                                        intent.putExtra("parkingLotId", parkingLotId);
                                        intent.putExtra("isDirectedGraph", 1);
                                        startActivity(intent);

                                    } catch (Exception ex){
                                        Log.e(App.TAG, ex.toString());
                                        Toast.makeText(App.getInstance(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(App.TAG, e.toString());
                                    Toast.makeText(App.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null);
        dialog.show();

        /*
        OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .method("POST", RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), jsonObject.toString()))
            .url("http://url/parse/functions/hello")
            .addHeader("X-Parse-Application-Id", "myAppId")
            .addHeader("X-Parse-REST-API-Key", "myApiKey")
            .addHeader("Content-Type", "application/json")
            .build();
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            if (e != null)
                e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.e("res", response.message());
        }
    });

         */
    }
}
