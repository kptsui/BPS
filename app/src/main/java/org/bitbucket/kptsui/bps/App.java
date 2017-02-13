package org.bitbucket.kptsui.bps;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by kptsui on 13/2/2017.
 */

public class App extends Application {
    public final static String TAG = "BPS";
    private static App instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("RAYW2_BPS")
                .clientKey("291fa14b5f8dc421c65f53ab9886edce")
                .server("http://opw0011.ddns.net:8000/parse/")   // '/' important after 'parse'
                .build());
    }

    public static App getInstance(){
        return instance;
    }
}
