package org.bitbucket.kptsui.bps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView userId, userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userId = (TextView) findViewById(R.id.userId);
        userName = (TextView) findViewById(R.id.userName);
        userEmail = (TextView) findViewById(R.id.userEmail);

        ParseUser user = ParseUser.getCurrentUser();
        userId.setText(user.getObjectId());
        userName.setText(user.getUsername());
        userEmail.setText(user.getEmail());
    }

    public void btnLogoutClicked(View v){
        ParseUser.logOut();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
