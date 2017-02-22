package org.bitbucket.kptsui.bps;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class NavigationActivity extends AppCompatActivity {

    public final static String BEACON_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    private BeaconManager beaconManager;
    private Region region;
    private String scanId;

    private Handler mHandler;

    public final static int PAYPAL_REQUEST_CODE = 999;
    // Test pay 10 dollars
    public final static int PAY = 10;
    // payment to this account
    public final static String paypal_client_id = "ASnyoDP9KsZ8yVdYCrRaSddOpMQ1utYPHE0GvbkdnD50eAihws3yspNgaMsuhDdw4P5rlsaduNodNszN";

    PayPalConfiguration paypal_config;
    Intent service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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
        /*beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override public void onEddystonesFound(List<Eddystone> eddystones) {
                Log.d(TAG, "Nearby Eddystone beacons: " + eddystones);
                for (Eddystone b : eddystones){
                    Log.d(TAG, "rssi: " + b.rssi + "\n" + b.url);
                }
            }
        });*/

        region = new Region("ranged region",
                UUID.fromString(BEACON_UUID), null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {

                Log.d(App.TAG, "Receive Beacons");

                try {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(App.getInstance(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void leave(View v){
        // TODO: invoke server API to get paymentAmount, then invoke pay(paymentAmount)
    }

    private void pay(double paymentAmount){
        PayPalPayment payment = new PayPalPayment(new BigDecimal(paymentAmount), "HKD", "Car Park Payment",
                PayPalPayment.PAYMENT_INTENT_SALE);
        // go to PaymentActivity
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypal_config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
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
