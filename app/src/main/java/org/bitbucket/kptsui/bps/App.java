package org.bitbucket.kptsui.bps;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.UUID;

/**
 * Created by kptsui on 13/2/2017.
 */

public class App extends Application {
    public final static String TAG = "BPS";
    public final static String BEACON_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    public final static int BEACON_ENTER_EXIT_COMMON_MINOR_ID = 99;
    public final static int BEACON_ENTER_MAJOR_ID = 0;
    public final static int BEACON_LIFT_MAJOR_ID = 98;
    public final static int BEACON_EXIT_MAJOR_ID = 99;

    public final static String PREFS_KEY = "BPS_USER";
    public final static String PREFS_USER_ID_KEY = "id";
    public final static String PREFS_USER_NAME_KEY = "name";
    public final static String PREFS_USER_PW_KEY = "pw";

    private static App instance;
    private SharedPreferences sharedPreferences;
    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());
        // Enter, Exit Beacons minor = 99, major = 0, 98, 99
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                if(User.getInstance().isLogged()
                    &&region.getMinor() == BEACON_ENTER_EXIT_COMMON_MINOR_ID){

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingSpace");

                    query.whereEqualTo("currentUser", User.getInstance().getId());
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        Log.d(App.TAG, "Fetched object: " + object);
                                        if(object != null){ // user parked before
                                            showNotification("Welcome Back", "Find your car lot?");
                                        }
                                        else {
                                            showNotification("Welcome to HKUST", "Park a car?");
                                        }
                                    } else {
                                        // something went wrong
                                        Log.e(App.TAG, e.toString());
                                    }
                                }
                            });
                }
            }

            @Override
            public void onExitedRegion(Region region) {
                if(User.getInstance().isLogged()
                        && region.getMinor() == BEACON_ENTER_EXIT_COMMON_MINOR_ID
                        && region.getMajor() == BEACON_EXIT_MAJOR_ID){

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingSpace");

                    query.whereEqualTo("currentUser", User.getInstance().getId());
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        Log.d(App.TAG, "Fetched object: " + object);
                                        if(object != null){ // user parked before
                                            showNotification("Goodbye", "Pay parking charge?");
                                        }
                                        else {
                                            showNotification("Goodbye", "You are leaving HKUST car park.");
                                        }
                                    } else {
                                        // something went wrong
                                        Log.e(App.TAG, e.toString());
                                    }
                                }
                            });
                }
            }
        });
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString(BEACON_UUID),
                        null, BEACON_ENTER_EXIT_COMMON_MINOR_ID));
            }
        });
        instance = this;

        this.sharedPreferences = getSharedPreferences(PREFS_KEY, 0);
        User user = User.getInstance();
        user.setId(sharedPreferences.getString(PREFS_USER_ID_KEY, null));
        user.setName(sharedPreferences.getString(PREFS_USER_NAME_KEY, null));
        user.setPw(sharedPreferences.getString(PREFS_USER_PW_KEY, null));

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("RAYW2_BPS")
                .clientKey("291fa14b5f8dc421c65f53ab9886edce")
                .server("http://opw0011.ddns.net:8000/parse/")   // '/' important after 'parse'
                .build());
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public static App getInstance(){
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
