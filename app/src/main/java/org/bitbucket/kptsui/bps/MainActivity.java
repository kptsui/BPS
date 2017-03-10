package org.bitbucket.kptsui.bps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String CAR_PARK_ID = "f1tMOZIDmZ";

    private TextView availableSpaces;
    private TextView totalSpaces;
    private TextView carParkName;
    private TextView carParkAddress;

    private ResideMenu resideMenu;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemSettings;

    private ActionBar actionBar;

    private RequestQueue requestQueue;

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
        itemSettings = new ResideMenuItem(this, R.drawable.ic_setting, "Settings");

        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);

        itemProfile.setOnClickListener(this);
        itemSettings.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {

        if (view == itemProfile){
            if(ParseUser.getCurrentUser() != null)
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        }else if (view == itemSettings){

        }

        resideMenu.closeMenu();
    }

    private void updateParkingSpaces(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CarPark");
        query.getInBackground(CAR_PARK_ID, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d(App.TAG, "Fetched object: " + object);

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

        query.whereEqualTo("currentUser", ParseUser.getCurrentUser().getObjectId());
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
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Getting Car Lot");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, "http://opw0011.ddns.net:1337/parse/functions/checkin",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(App.TAG, response);

                            Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                            intent.putExtra("isDirectedGraph", 1);
                            startActivity(intent);

                        } catch (Exception e){
                            Log.e(App.TAG, e.getMessage());
                            Toast.makeText(App.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e(App.TAG, error.getMessage());
                        Toast.makeText(App.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("X-Parse-Application-Id", "RAYW2_BPS");
                params.put("X-Parse-REST-API-Key", "291fa14b5f8dc421c65f53ab9886edce");
                params.put("X-Parse-Session-Token", "");

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };
        requestQueue.add(request);
    }
}
