package org.bitbucket.kptsui.bps;

import android.os.Bundle;
import android.app.Activity;

public class SpaceShareActivity extends Activity {

    //WeekView mWeekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_share);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get a reference for the week view in the layout.
        //mWeekView = (WeekView) findViewById(R.id.weekView);

        // Set an action when any event is clicked.
        //mWeekView.setOnEventClickListener(mEventClickListener);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        //mWeekView.setMonthChangeListener(mMonthChangeListener);

        // Set long press listener for events.
        //mWeekView.setEventLongPressListener(mEventLongPressListener);
    }

}
