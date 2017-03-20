package org.bitbucket.kptsui.bps;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
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
import com.parse.ParseUser;
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

    private WebView webView;
    private int isDirectedGraph;
    private int showMyPointOnly;
    private String parkingLotId = "p55";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        getSupportActionBar().setElevation(0);

        webView = (WebView) findViewById(R.id.webView);
        // background color must be set in code in order to prevent white loading screen
        webView.setBackgroundColor(getResources().getColor(R.color.backgroundFloorPlanPurpleBlue));
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //mWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
        webView.loadUrl("file:///android_asset/index.html");

        parkingLotId = getIntent().getStringExtra("parkingLotId");
        isDirectedGraph = getIntent().getIntExtra("isDirectedGraph", 0);

        TextView userParkingLot = (TextView) findViewById(R.id.userParkingLot);
        userParkingLot.setText(parkingLotId);

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
                        /* Test
                        final String invoke =
                                "javascript:updateMarker(" + showMyPointOnly +
                                        "," + isDirectedGraph + ",'p55','"
                                        + "[{\"bid\": 22, \"rssi\": -55}]" + "');";
                        Log.d(App.TAG, invoke);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl(invoke);
                            }
                        });
                        */
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

                    final String invoke =
                            "javascript:updateMarker(" + showMyPointOnly +
                                    "," + isDirectedGraph +
                                    ",'p55','" + jsonArray.toString() + "');";

                    Log.d(App.TAG, invoke);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(invoke);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.navigation:
                isDirectedGraph = 1;
                return true;

            case R.id.walk:
                isDirectedGraph = 0;
                return true;

            case R.id.showPositionOnly:
                if(showMyPointOnly == 0)
                    showMyPointOnly = 1;
                else
                    showMyPointOnly = 0;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

}
