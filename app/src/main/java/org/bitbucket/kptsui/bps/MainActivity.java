package org.bitbucket.kptsui.bps;

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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

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
            if(User.getInstance().isLogged())
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
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingSpace");

        Log.d(App.TAG, "ParkingSpace user id: " + User.getInstance().getId());

        query.whereEqualTo("currentUser", User.getInstance().getId());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) { // user parked before
                    Log.d(App.TAG, "Fetched object: " + object);
                    String parkingLotId = object.getString("parkingLotId");

                    Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                    intent.putExtra("parkingLotId", parkingLotId);
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
        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
        startActivity(intent);
    }
}
