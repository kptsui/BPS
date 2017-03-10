package org.bitbucket.kptsui.bps;

import android.app.Activity;
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
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
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

    // Paypal variable
    public final static int PAYPAL_REQUEST_CODE = 999;
    // Test pay 10 dollars
    private int hourlyRate = 40;
    // payment to this account
    public final static String paypal_client_id = "ASnyoDP9KsZ8yVdYCrRaSddOpMQ1utYPHE0GvbkdnD50eAihws3yspNgaMsuhDdw4P5rlsaduNodNszN";

    PayPalConfiguration paypal_config;
    Intent service;

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

        //PayPal
        // Sandbox for test, Production for real
        paypal_config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(paypal_client_id);

        service = new Intent(this, PayPalService.class);
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
        startService(service); // listening

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
        /*final ProgressDialog progressDialog = new ProgressDialog(this);
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
                params.put("X-Parse-Session-Token", ParseUser.getCurrentUser().getSessionToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject json = new JSONObject();
                try {
                    json.put("time", 5);
                    return json.toString().getBytes();

                } catch (JSONException e) {
                    e.printStackTrace();
                    return "{}".getBytes();
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        requestQueue.add(request);
        */

        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
        intent.putExtra("isDirectedGraph", 1);
        startActivity(intent);
    }

    // should only be invoked by onActivityResult
    private void checkout(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Checking out");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, "http://opw0011.ddns.net:1337/parse/functions/checkout",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Toast.makeText(App.getInstance(), "Check-out successfully", Toast.LENGTH_SHORT).show();
                            Log.d(App.TAG, response);

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
                params.put("X-Parse-Session-Token", ParseUser.getCurrentUser().getSessionToken());
                return params;
            }
        };
        requestQueue.add(request);
    }

    // TODO: invoke this method when user click check-out
    private void pay(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingRecord");

        query.whereEqualTo("status", "checkedIn");
        query.whereEqualTo("user", ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d(App.TAG, "Fetched object: " + object);
                    if(object != null){ // user parked before
                        Date checkIn = object.getDate("checkinTime");
                        Date checkOut = object.getDate("checkoutTime");
                        long parkedHours = (checkOut.getTime() - checkIn.getTime()) / (60*60*1000);

                        PayPalPayment payment = new PayPalPayment(new BigDecimal(parkedHours * hourlyRate), "HKD", parkedHours + " hr(s) Car Park Payment",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        // go to PaymentActivity
                        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

                        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                    }
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.toString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirm != null){
                    String state = confirm.getProofOfPayment().getState();
                    if(state.equals("approved")){
                        Toast.makeText(this, "Payment is approved", Toast.LENGTH_SHORT).show();
                        Log.d(App.TAG, "Payment is approved");

                        // Server data check out here
                        checkout();
                    }
                    else {
                        Toast.makeText(this, "Payment error: " + state, Toast.LENGTH_SHORT).show();
                        Log.e(App.TAG, "Payment error: " + state);
                    }
                }
                else {
                    Toast.makeText(this, "Confirmation is null", Toast.LENGTH_SHORT).show();
                    Log.e(App.TAG, "Confirmation is null");
                }
            }
        }
    }
}
