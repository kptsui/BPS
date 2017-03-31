package org.bitbucket.kptsui.bps;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Record> records;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        records = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // enable this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new RecordAdapter(this, records);
        mRecyclerView.setAdapter(mAdapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingRecord");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                try {
                    Log.d(App.TAG, objects.toString());
                    if (e == null) {
                        for(ParseObject object : objects){
                            Record record = null;
                            record = new Record(
                                    object.getParseObject("parkingSpace").fetchIfNeeded().getString("parkingLotId"),
                                    object.getString("paymentAmount"),
                                    object.getDate("checkinTime"),
                                    object.getDate("checkoutTime")
                            );
                            records.add(record);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        // something went wrong
                        Log.e(App.TAG, e.toString());
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    progressBar.setVisibility(View.GONE);
                }

            }
        });
    }
}
