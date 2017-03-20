package org.bitbucket.kptsui.bps;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.Gson;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpaceShareActivity extends AppCompatActivity {

    WeekView mWeekView;
    ArrayList<MainActivity.Schedule> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_share);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // Set an action when any event is clicked.
        //mWeekView.setOnEventClickListener(mEventClickListener);

        // The week view has infinite scrolling horizontally. We must provide the events of a
        // month every time the month changes on the week view.
        schedules = new ArrayList<>();
        ArrayList<String> schedulesJSONs = getIntent().getStringArrayListExtra("schedules");
        for(String json : schedulesJSONs){
            schedules.add(new Gson().fromJson(json, MainActivity.Schedule.class));
        }

        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                Log.d(App.TAG, "newMonth: " + newMonth);

                List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

                for(MainActivity.Schedule schedule : schedules){
                    Log.d(App.TAG, new Gson().toJson(schedule));

                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, schedule.startTime);
                    startTime.set(Calendar.MINUTE, 0);
                    startTime.set(Calendar.MONTH, newMonth-1); // 0 = January = Calendar.JANUARY
                    startTime.set(Calendar.YEAR, newYear);
                    startTime.set(Calendar.DAY_OF_WEEK, schedule.weekday + 1); // 1 = Sunday

                    Calendar endTime = (Calendar) startTime.clone();
                    endTime.set(Calendar.HOUR_OF_DAY, schedule.startTime + schedule.duration);
                    endTime.set(Calendar.MONTH, newMonth-1);
                    endTime.set(Calendar.DAY_OF_WEEK, schedule.weekday + 1);

                    WeekViewEvent event = new WeekViewEvent(
                            schedules.indexOf(schedule),
                            schedule.parkingLotId,
                            startTime,
                            endTime);

                    event.setColor(ContextCompat.getColor(SpaceShareActivity.this, R.color.scheduleGreen));
                    events.add(event);
                }

                return events;
            }
        });

        // Set long press listener for events.
        //mWeekView.setEventLongPressListener(mEventLongPressListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseParkingTime();
            }
        }).start();
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    private void releaseParkingTime(){
        String temp = "{ \"parkingSpaceId\": \"xvZOxGKTjR\", \"schedule\": [ { \"weekday\": 2, \"startTime\": 13, \"duration\": 4 }, { \"weekday\": 3, \"startTime\": 18, \"duration\": 2 } ] }";
        try {
            Log.d(App.TAG, post(temp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String post(String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader("X-Parse-Application-Id", "RAYW2_BPS")
                .addHeader("X-Parse-REST-API-Key", "187828839bd2db4373674422c649de2b")
                .addHeader("X-Parse-Session-Token", ParseUser.getCurrentUser().getSessionToken())
                .url("http://opw0011.ddns.net:8000/parse/functions/release")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
