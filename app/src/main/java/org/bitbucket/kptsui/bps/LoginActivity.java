package org.bitbucket.kptsui.bps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import info.hoang8f.android.segmented.SegmentedGroup;

public class LoginActivity extends AppCompatActivity {

    EditText loginName;
    EditText loginPw;
    RadioButton rbtnLogin;
    Button btnLoginSend;

    EditText signUpEmail;
    EditText signUpName;
    EditText signUpPw;
    EditText signUpPw2;
    RadioButton rbtnSignUp;
    Button btnSignUpSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SegmentedGroup segmented = (SegmentedGroup) findViewById(R.id.segmentedGroup);
        segmented.setTintColor(getResources().getColor(R.color.colorAccent));

        loginName = (EditText) findViewById(R.id.loginName);
        loginPw = (EditText) findViewById(R.id.loginPw);
        btnLoginSend = (Button) findViewById(R.id.btnLoginSend);

        signUpEmail = (EditText) findViewById(R.id.signUpEmail);
        signUpName = (EditText) findViewById(R.id.signUpName);
        signUpPw = (EditText) findViewById(R.id.signUpPw);
        signUpPw2 = (EditText) findViewById(R.id.signUpPw2);
        btnSignUpSend = (Button) findViewById(R.id.btnSignUpSend);

        rbtnLogin = (RadioButton) findViewById(R.id.rbtnLogin);
        rbtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginName.setVisibility(View.VISIBLE);
                loginPw.setVisibility(View.VISIBLE);
                btnLoginSend.setVisibility(View.VISIBLE);

                signUpEmail.setVisibility(View.GONE);
                signUpName.setVisibility(View.GONE);
                signUpPw.setVisibility(View.GONE);
                signUpPw2.setVisibility(View.GONE);
                btnSignUpSend.setVisibility(View.GONE);
            }
        });

        rbtnSignUp = (RadioButton) findViewById(R.id.rbtnSignUp);
        rbtnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginName.setVisibility(View.GONE);
                loginPw.setVisibility(View.GONE);
                btnLoginSend.setVisibility(View.GONE);

                signUpEmail.setVisibility(View.VISIBLE);
                signUpName.setVisibility(View.VISIBLE);
                signUpPw.setVisibility(View.VISIBLE);
                signUpPw2.setVisibility(View.VISIBLE);
                btnSignUpSend.setVisibility(View.VISIBLE);
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    public void btnLoginSend(View v){
        login(loginName.getText().toString(), loginPw.getText().toString());
    }

    private void login(final String name, final String pw){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ParseUser.logInInBackground(name, pw, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();

                if (user != null) {
                    // Hooray! The user is logged in.
                    Toast.makeText(App.getInstance(), "Login succeed", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Toast.makeText(App.getInstance(), "Login failed", Toast.LENGTH_SHORT).show();
                    Log.e(App.TAG, e.getMessage());
                }
            }
        });
    }

    public void btnSignUpSend(View v){
        signUp(signUpName.getText().toString(),
                signUpPw.getText().toString(),
                signUpEmail.getText().toString());
    }

    private void signUp(final String name, final String pw, final String email){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Signing Up");
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        ParseUser user = new ParseUser();
        user.setUsername(name);
        user.setPassword(pw);
        user.setEmail(email);

        // other fields can be set just like with ParseObject
        // eg.
        //user.put("phone", "650-253-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                progressDialog.dismiss();

                if (e == null) {
                    // Hooray! Let them use the app now.
                    Toast.makeText(App.getInstance(), "Sign up succeed", Toast.LENGTH_SHORT).show();
                    login(name, pw);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.e(App.TAG, e.getMessage());
                    Toast.makeText(App.getInstance(), "Sign up failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
