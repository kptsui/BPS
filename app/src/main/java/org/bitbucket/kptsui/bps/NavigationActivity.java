package org.bitbucket.kptsui.bps;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

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
}
