package com.alainsaris.stitcha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity<mCallbackManager> extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;

    private Button mRegBtn;
    private Button mLoginBtn;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CallbackManager mCallbackManager;
    private String TAG = StartActivity.class.getName();
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private ProgressBar progressBar;

    //firebase
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //firebasec: initialize auth and analytics
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //ui

        //facebook login
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.buttonFacebookLogin);
        progressBar = findViewById(R.id.start_progressBar);
        progressBar.setVisibility(ProgressBar.GONE);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
//                .setAccountName("Anon")
                .build();
        // [END config_signin]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //google sign in btn
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);


        /**
         * old sign in method: email password
         * old regiser method: ...
         */
//        mRegBtn = findViewById(R.id.start_reg_btn);
//        mLoginBtn = findViewById(R.id.start_login_btn);
//
//        mRegBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
//                startActivity(registerIntent);
//            }
//        });
//        mLoginBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
//                startActivity(loginIntent);
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            //if the user is already signed in just send him to MapAcitivty
            sendToMain();
        }
    }

    //google sign in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //google stuff
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                loadingEnd();
                // ...
            }
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
//        Toast.makeText(this, "handeling facebook token!", Toast.LENGTH_SHORT).show();
        //ProgressBar Stuff
        progressBar.setVisibility(ProgressBar.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //progressBar stuff end
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        finalLoginStep(credential);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//                            //send the user to the main acitivity(MapActivity)
//                            currentUser = mAuth.getCurrentUser();
//                            updateUI(currentUser);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(StartActivity.this, "Authentication failed:" + task.getException(),
//                                    Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(ProgressBar.GONE);
//                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
////                            updateUI(null);
//                        }
//
//                        // ...
//                    }
//                });
    }

    private void finalLoginStep(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            final FirebaseUser user = mAuth.getCurrentUser();
                            currentUser = mAuth.getCurrentUser();
//                            Toast.makeText(StartActivity.this, "got the current user dara: " + user, Toast.LENGTH_SHORT).show();
//                            sendToMain();
                            final FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userMap = new HashMap<>();
//                            userMap.put("current location", new GeoPoint(0, 0));
                            userMap.put("email", currentUser.getEmail());
                            userMap.put("id", currentUser.getUid());
                            Log.d(TAG, "onComplete: finalLoginStep: " + currentUser.getUid());
                            Log.d(TAG, "onComplete: finalLoginStep:  " + mAuth.getCurrentUser().getUid());
                            userMap.put("last logged in", FieldValue.serverTimestamp());
                            userMap.put("logged in", true);
                            userMap.put("name", currentUser.getDisplayName());
                            userMap.put("phone number", "0000000000");
//                            userMap.put("points", 9001);
//                            userMap.put("profie pic", currentUser.getPhotoUrl());
                            db.collection("users").document(mAuth.getUid()).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String, Object> userSettingsMap = new HashMap<>();
                                    userSettingsMap.put("show profile pic", true);
                                    userSettingsMap.put("show name", true);
                                    userSettingsMap.put("profile public", true);
                                    userSettingsMap.put("share position", false);
                                    db.collection("users").document(mAuth.getUid()).collection("settings").document("settings")
                                            .set(userSettingsMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            updateUI(currentUser);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(StartActivity.this, "couldn' write userSettings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingEnd();
                                            updateUI(null);
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(StartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    loadingEnd();
                                    updateUI(null);
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "failed to login: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loadingEnd();
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    //send to the MapActivity
    private void sendToMain() {
        Intent mainIntent = new Intent(StartActivity.this, MapActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                signIn();
                break;
//             ...
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }


    //google shit
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        loadingStart();
    }

    //show a loading progress bar and stop user interactivity
    private void loadingStart() {
        //ProgressBar Stuff
        progressBar.setVisibility(ProgressBar.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //progressBar stuff end
    }

    //hide loading progress bar and restore user interactivity
    private void loadingEnd() {
        progressBar.setVisibility(ProgressBar.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        finalLoginStep(credential);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
////                            final FirebaseUser user = mAuth.getCurrentUser();
//                            currentUser = mAuth.getCurrentUser();
////                            Toast.makeText(StartActivity.this, "got the current user dara: " + user, Toast.LENGTH_SHORT).show();
////                            sendToMain();
//                            final FirebaseFirestore db = FirebaseFirestore.getInstance();
//                            Map<String, Object> userMap = new HashMap<>();
////                            userMap.put("current location", new GeoPoint(0, 0));
//                            userMap.put("email", currentUser.getEmail());
//                            userMap.put("id", currentUser.getUid());
//                            userMap.put("last logged in", FieldValue.serverTimestamp());
//                            userMap.put("logged in", true);
//                            userMap.put("name", currentUser.getDisplayName());
//                            userMap.put("phone number", "0000000000");
////                            userMap.put("points", 9001);
////                            userMap.put("profie pic", currentUser.getPhotoUrl());
//                            db.collection("users").document(mAuth.getUid()).set(userMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Map<String, Object> userSettingsMap = new HashMap<>();
//                                    userSettingsMap.put("show profile pic", true);
//                                    userSettingsMap.put("show name", true);
//                                    userSettingsMap.put("profile public", true);
//                                    userSettingsMap.put("share position", false);
//                                    db.collection("users").document(mAuth.getUid()).collection("settings").document("settings")
//                                            .set(userSettingsMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            updateUI(currentUser);
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(StartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(StartActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    mAuth.signOut();
//                                }
//                            });
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            loadingEnd();
////                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
//                        }
//
//                        // [START_EXCLUDE]
////                        hideProgressDialog();
//                        // [END_EXCLUDE]
//                    }
//                });
    }

    //this is not really necessary because the launch activity is the MapActivity which will ask for permissions everytime
    //we still want to let the user sign up or login even without him giving us the location permission
    public void requestPermissionsWithDexter() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        //as long as the user has permissions this part will be called
                        //wether it's the first time he get the permissions or not!!
//                        Toast.makeText(MapActivity.this, "the permissions are granted!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(StartActivity.this, "you must enable permission!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    // [END auth_with_google]
}