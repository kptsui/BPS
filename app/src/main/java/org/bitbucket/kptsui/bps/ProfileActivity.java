package org.bitbucket.kptsui.bps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    public void btnLogoutClicked(View v){
        User.getInstance().clean();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
    }
}
