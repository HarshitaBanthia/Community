package com.example.hp.community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NextActivity extends AppCompatActivity {
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        sp=getSharedPreferences("login-data",MODE_PRIVATE);
        spe=sp.edit();
        Toast.makeText(this, sp.getString("username"," ")+" "+sp.getString("name"," "), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.mGoogleApiClient.connect();
    }

    public void signOut(View view)
    {
        if(MainActivity.Re==0)
        LoginManager.getInstance().logOut();
        revokeAccess();
    }
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(MainActivity.mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                        Toast.makeText(NextActivity.this, "signed out", Toast.LENGTH_SHORT).show();
                        spe.clear();
                        spe.commit();
                        MainActivity.Re=-1;
                        FirebaseAuth.getInstance().signOut();
                        Intent i=new Intent(NextActivity.this,MainActivity.class);
                        finish();
                        startActivity(i);
                    }
                });
    }

}
