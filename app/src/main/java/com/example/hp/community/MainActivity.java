package com.example.hp.community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    GoogleSignInOptions gso;
    public static  GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_S = 9001;
    private FirebaseAuth mAuth;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    LoginButton loginButton;
    private CallbackManager mCallbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp=getSharedPreferences("login-data",MODE_PRIVATE);
        spe=sp.edit();
        if(sp.getBoolean("email",false))
        {
            Intent i=new Intent(this,NextActivity.class);
            finish();
            startActivity(i);
        }
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("937138870846-d7e0o7t647tusqmv1olvuk6aprduuggj.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Error connecting", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.stopAutoManage(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }*/
    public void gShow(View view)
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    public void fsh(View view)
    {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_photos", "email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        String email="";
                                        final long fb_id;
                                        try {
                                            fb_id = object.getLong("id");

                                            final String name=object.getString("name");
                                            email=object.getString("email");
                                            spe.putBoolean("email",true);
                                            spe.putString("username",email);
                                            spe.putString("name",name);
                                            spe.commit();
                                            Intent i=new Intent(MainActivity.this,NextActivity.class);
                                            finish();
                                            startActivity(i);
                                            Toast.makeText(MainActivity.this, fb_id+"\n"+name+"\n"+email, Toast.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel()
                    {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        // App code
                    }
                });
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately

                Log.e("3",result.getStatus().toString());
                Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
               }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e("1", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("2", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            spe.putBoolean("email",true);
                            spe.putString("username",user.getEmail());
                            spe.putString("name",user.getDisplayName());
                            spe.commit();
                            Intent i=new Intent(MainActivity.this,NextActivity.class);
                            finish();
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("4", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                         }
                    }
                });
    }
}


