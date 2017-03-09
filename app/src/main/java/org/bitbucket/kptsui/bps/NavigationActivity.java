package org.bitbucket.kptsui.bps;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.eddystone.Eddystone;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import static org.bitbucket.kptsui.bps.App.TAG;
import static org.bitbucket.kptsui.bps.App.BEACON_UUID;

public class NavigationActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private String scanId;

    private Handler mHandler;

    public final static int PAYPAL_REQUEST_CODE = 999;
    // Test pay 10 dollars
    public final static int HOURLY_RATE = 40;
    // payment to this account
    public final static String paypal_client_id = "ASnyoDP9KsZ8yVdYCrRaSddOpMQ1utYPHE0GvbkdnD50eAihws3yspNgaMsuhDdw4P5rlsaduNodNszN";

    PayPalConfiguration paypal_config;
    Intent service;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //mWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
        webView.loadUrl("file:///android_asset/index.html");

        // TODO:
        String parkingLotId = getIntent().getStringExtra("parkingLotId");

        // Sandbox for test, Production for real
        paypal_config = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(paypal_client_id);

        service = new Intent(this, PayPalService.class);
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
        startService(service); // listening

        mHandler = new Handler();

        beaconManager = new BeaconManager(this);
        // Should be invoked in #onCreate.
        beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override public void onEddystonesFound(List<Eddystone> eddystones) {
                Log.d(TAG, "Nearby Eddystone beacons: " + eddystones);
                for (Eddystone b : eddystones){
                    Log.d(TAG, "rssi: " + b.rssi + "\n" + b.url);
                }
            }
        });

        region = new Region("ranged region",
                UUID.fromString(BEACON_UUID), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                try {
                    if(beacons == null || beacons.size() == 0){
                        Log.d(App.TAG, "no Beacons found");
                        return;
                    }

                    Log.d(App.TAG, "Receive Beacons");

                    JSONArray jsonArray = new JSONArray();
                    for(Beacon b : beacons){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("bid", b.getMajor());
                        jsonObject.put("rssi", b.getRssi());

                        jsonArray.put(jsonObject);
                    }

                    final StringBuilder invoke = new StringBuilder();
                    invoke.append("javascript:updateMarker('p55','")
                            .append(jsonArray.toString())
                            .append("');");

                    Log.d(App.TAG, invoke.toString());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(invoke.toString());
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(App.getInstance(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Should be invoked in #onStart.
        /*beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                scanId = beaconManager.startEddystoneScanning();
            }
        });*/

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onStop() {
        beaconManager.stopRanging(region);
        super.onStop();

        // Should be invoked in #onStop.
        //beaconManager.stopEddystoneScanning(scanId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When no longer needed. Should be invoked in #onDestroy.
        beaconManager.disconnect();
    }

    public void leave(View v){
        // TODO: invoke server API to get paymentAmount, then invoke pay(paymentAmount)
    }

    private void pay(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingRecord");

        query.whereEqualTo("status", "checkedOut");
        query.whereEqualTo("user", User.getInstance().getId());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d(App.TAG, "Fetched object: " + object);
                    if(object != null){ // user parked before
                        Date checkIn = object.getDate("checkinTime");
                        Date checkOut = object.getDate("checkoutTime");
                        long parkedHours = (checkOut.getTime() - checkIn.getTime()) / (60*60*1000);

                        PayPalPayment payment = new PayPalPayment(new BigDecimal(parkedHours * HOURLY_RATE), "HKD", "Car Park Payment",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        // go to PaymentActivity
                        Intent intent = new Intent(NavigationActivity.this, PaymentActivity.class);
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
