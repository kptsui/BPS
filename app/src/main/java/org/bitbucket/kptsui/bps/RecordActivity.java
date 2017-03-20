package org.bitbucket.kptsui.bps;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class RecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching your status");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        // Fetch hourlyRate
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParkingRecord");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                Log.d(App.TAG, objects.toString());
                progressDialog.dismiss();

                if (e == null) {
                    for(ParseObject object : objects){

                    }
                } else {
                    // something went wrong
                    Log.e(App.TAG, e.toString());
                }
            }
        });
    }
}
