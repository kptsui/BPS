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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String CAR_PARK_ID = "f1tMOZIDmZ";

    private TextView availableSpace;

    private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemCalendar;
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

        availableSpace = (TextView) findViewById(R.id.availableSpace);

        updateParkingSpaces();
    }

    private void setUpSideMenu(){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);

        resideMenu.setScaleValue(0.5f);
        //resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        // create menu items;
        itemHome     = new ResideMenuItem(this, R.drawable.abc_btn_check_material,     "Home");
        itemProfile  = new ResideMenuItem(this, R.drawable.abc_btn_check_material,  "Profile");
        itemCalendar = new ResideMenuItem(this, R.drawable.abc_btn_check_material, "Calendar");
        itemSettings = new ResideMenuItem(this, R.drawable.abc_btn_check_material, "Settings");

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCalendar, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);

        itemHome.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemCalendar.setOnClickListener(this);
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

        if (view == itemHome){

        }else if (view == itemProfile){
            if(User.getInstance().isLogged())
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        }else if (view == itemCalendar){

        }else if (view == itemSettings){

        }

        resideMenu.closeMenu();
    }

    private void updateParkingSpaces(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CarPark");
        query.getInBackground(CAR_PARK_ID, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d(App.TAG, "Fetch object: " + object);
                    int availableSpace = object.getInt("availableSpace");
                    MainActivity.this.availableSpace.setText(String.valueOf(availableSpace));
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.toString());
                }
            }
        });
    }

    private void toNavigationActivity(){
        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
        startActivity(intent);
    }

    public void onNavigationClick(View v){
        toNavigationActivity();
    }
}
