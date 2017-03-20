package org.bitbucket.kptsui.bps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userEmail, avgHours, parkedHours, parkingLot, timer, checkinTime;

    private int hourlyRate = 0;

    // Paypal variable
    public final static int PAYPAL_REQUEST_CODE = 999;

    // payment to this account
    public final static String paypal_client_id = "ASnyoDP9KsZ8yVdYCrRaSddOpMQ1utYPHE0GvbkdnD50eAihws3yspNgaMsuhDdw4P5rlsaduNodNszN";

    PayPalConfiguration paypal_config;
    Intent service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.profile_header_background));

        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);

        avgHours = (TextView) findViewById(R.id.avgHours);
        parkedHours = (TextView) findViewById(R.id.parkedHours);

        parkingLot = (TextView) findViewById(R.id.parkingLot);
        timer = (TextView) findViewById(R.id.timer);
        checkinTime = (TextView) findViewById(R.id.checkinTime);

        ParseUser user = ParseUser.getCurrentUser();

        userName.setText(user.getUsername());
        userEmail.setText(user.getEmail());

        //PayPal
        // Sandbox for test, Production for real
        paypal_config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(paypal_client_id);

        service = new Intent(this, PayPalService.class);
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
        startService(service); // listening

        getInfo();
    }

    private void getInfo(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching your status");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        // Fetch hourlyRate
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CarPark");
        query.getInBackground(MainActivity.CAR_PARK_ID, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    hourlyRate = object.getInt("hourlyRate");
                    Log.d(App.TAG, "Fetched hourlyRate: " + hourlyRate);

                    // Fetch parkingLot ID and status
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingSpace");
                    query.whereEqualTo("currentUser", ParseUser.getCurrentUser());
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            try {
                                if (e == null) {
                                    if(object != null){
                                        String parkingLotId = object.getString("parkingLotId");
                                        String status = object.getString("status");

                                        parkingLot.setText(parkingLotId);
                                    }
                                } else {
                                    // something went wrong
                                    Log.e(App.TAG, e.toString());
                                }
                            } catch (Exception ex){
                                Log.e(App.TAG, ex.toString());
                                Toast.makeText(App.getInstance(), ex.toString(), Toast.LENGTH_SHORT).show();

                            } finally {
                                progressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    // something went wrong
                    progressDialog.dismiss();
                    Log.e(App.TAG, e.toString());
                }
            }
        });
    }

    public void btnLogoutClicked(View v){
        ParseUser.logOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void btnCheckoutClicked(View v){
        // TODO: invoke pay() only after testing
        //pay();
        checkout();
    }

    private void pay(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Checking your status");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingRecord");

        query.whereEqualTo("status", "checkedIn");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                progressDialog.dismiss();

                if (e == null) {
                    if(hourlyRate == 0){
                        Log.e(App.TAG, "hourlyRate not set correctly ");
                        Toast.makeText(App.getInstance(), "Info. no be set correctly", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(object != null){ // user parked before
                        Date checkIn = object.getDate("checkinTime");
                        Date checkOut = new Date();
                        long diff = checkOut.getTime() - checkIn.getTime();
                        // TimeUnit.MILLISECONDS.toHours(diff);
                        float parkedHours = (float) diff / (3600000); // 60*60*1000

                        Log.d(App.TAG, "checkOut.getTime(): " + checkOut.getTime());
                        Log.d(App.TAG, "checkIn.getTime(): " + checkIn.getTime());
                        Log.d(App.TAG, "parkedHours: " + parkedHours);

                        parkedHours = parkedHours < 1 ? 1 : parkedHours;

                        PayPalPayment payment = new PayPalPayment(new BigDecimal(parkedHours * hourlyRate), "HKD", "Car Park Payment",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        // go to PaymentActivity
                        Intent intent = new Intent(ProfileActivity.this, PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

                        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                    }
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.toString());
                    Toast.makeText(App.getInstance(), e.toString(), Toast.LENGTH_SHORT).show();
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

    // should only be invoked by onActivityResult
    private void checkout(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Checking out");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ParseCloud.callFunctionInBackground("checkout", new HashMap<String, Object>(), new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                progressDialog.dismiss();

                if (e == null) {
                    Toast.makeText(App.getInstance(), "Check-out succeeded", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(App.TAG, "No record found. You cannot checkout at the moment.");
                    Toast.makeText(App.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
