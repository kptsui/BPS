package org.bitbucket.kptsui.bps;

import android.app.Application;
import android.content.SharedPreferences;

import com.parse.Parse;

/**
 * Created by kptsui on 13/2/2017.
 */

public class App extends Application {
    public final static String TAG = "BPS";
    public final static String PREFS_KEY = "BPS_USER";
    public final static String PREFS_USER_NAME_KEY = "name";
    public final static String PREFS_USER_PW_KEY = "pw";

    private static App instance;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        this.sharedPreferences = getSharedPreferences(PREFS_KEY, 0);
        User user = User.getInstance();
        user.setName(sharedPreferences.getString(PREFS_USER_NAME_KEY, null));
        user.setPw(sharedPreferences.getString(PREFS_USER_PW_KEY, null));

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("RAYW2_BPS")
                .clientKey("291fa14b5f8dc421c65f53ab9886edce")
                .server("http://opw0011.ddns.net:8000/parse/")   // '/' important after 'parse'
                .build());
    }

    public static App getInstance(){
        return instance;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
