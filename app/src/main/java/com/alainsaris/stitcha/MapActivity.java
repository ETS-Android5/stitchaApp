package com.alainsaris.stitcha;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.login.LoginManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Math.pow;
import static java.lang.Math.round;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnCompleteListener<Void>,
        GoogleMap.OnMarkerClickListener, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MAP_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static Application sApplication;
    boolean rewardedAdShown = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    //admob
    private InterstitialAd mInterstitialAd;
    private AdView adView;
    boolean rewardedAdEnabled;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    ListenerRegistration stationVoteRegistration;
    ListenerRegistration unsafeVotersRegistration;
    ListenerRegistration unsafeVotesRegistration;

    // user settings
    private Map<String, Object> userSettingsMap = new HashMap<>();
    // ...
    String facebookUserId = "";

    //ui
    private Button mBtnChat;
    private ImageView profilePic;
    //    private Button mBtnSafe;
    private Button mBtnUnsafe;
    private Button mBtnAddGeofences;
    private Toolbar mToolbar;
    private TextView selectedStationTextView;
    private TextView currentStationTextView;
    private TextView currentGeoStateTextView;
    private TextView unsafeVotesTextView;
    FloatingActionButton feedBtn;

    SupportMapFragment mapFragment;

    //map
    //private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private CameraPosition mCameraPosition;
    private static final int DEFAULT_ZOOM = 15;
    private String selectedStation;
    private String currentStation;
    private String currentCity;

    // button
    private ImageView addMarkerBtn;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //geofencing
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private boolean geofencesAdded;
    private GeoFire geoFire;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String currentuser;

    //clicking functionallity and voting
    private boolean safeBtnClicked;
    private boolean unsafeBtnClicked;
    private int safeVotes;
    private int unsafeVotes;
    private HashMap<String, Object> votesHashMap;
    private RewardedAd rewardedAd;
    private boolean mapStyleDark;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                Intent intentToProfile = new Intent(MapActivity.this, SettingsActivity2.class);
                startActivity(intentToProfile);
                break;
            case R.id.nav_settings:
                Intent intentToSettings = new Intent(MapActivity.this, SettingsActivity.class);
                startActivity(intentToSettings);
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                break;
            case R.id.nav_rate:
                Toast.makeText(MapActivity.this, "5 stars plz XD", Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_marker_btn:
//                Toast.makeText(this, "popup", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MapActivity.this, MarkerChooserPopup.class);
                startActivityForResult(intent, 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                addMarker(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void addMarker(String result) {
        int drawableId = R.drawable.markers_fire_marker;
        String message = "alert!";
        switch (result) {
            case "policeMarker":
                drawableId = R.drawable.markers_police_marker;
                message = "Police!";
                break;
            case "ticketsMarker":
                drawableId = R.drawable.markers_ticket_marker;
                message = "Tickets!";
                break;
            case "fireMarker":
                drawableId = R.drawable.markers_fire_marker;
                message = "Fire!!!";
                break;
            case "protestMarker":
                drawableId = R.drawable.markers_protests_marker;
                message = "Protests!";
                break;
        }


        int height = 100;
        int width = 100;
        Bitmap b = BitmapFactory.decodeResource(getResources(), drawableId);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
        LatLng markerPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(markerPosition)
                .title(message)
                .icon(markerIcon);
        mMap.addMarker(markerOptions);
    }


    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    //firebase admin
    private HashMap<String, Object> stationsNames;
    private FirebaseUser currentUser;

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ///make status bar transparent
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        requestPermissionsWithDexter();
        mapStyleDark = true;

        //snapshots
        stationVoteRegistration = null;
        unsafeVotersRegistration = null;
        unsafeVotesRegistration = null;

        // user settings
//        userSettingsMap.put("profile pic", true);
        userSettingsMap.put("share position", false);
        userSettingsMap.put("get position", true);
        userSettingsMap.put("show name", true);
        userSettingsMap.put("show profile pic", true);
        getUserSettings();

        //admob
        //banner
        rewardedAdEnabled = false;
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(String.valueOf(R.string.admob_banner_ad));
        // TODO: Add adView to your view hierarchy.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        //interstitial
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_ad));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        addMarkerBtn = findViewById(R.id.map_marker_btn);
        addMarkerBtn.setOnClickListener(this);

        //native ad
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdLoader adLoader = new AdLoader.Builder(MapActivity.this, "ca-app-pub-3940256099942544/2247696110")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // Show the ad.
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAds(new AdRequest.Builder().build(), 3);
        //rewarded
        rewardedAd = new RewardedAd(this,
                getString(R.string.rewarded_ad_id));
        rewardedAd = createAndLoadRewardedAd();
        navigationView = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);


        currentCity = "lyon";

//        requestPermissions();


//        Dexter.withActivity(this)
//                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse response) {
//                        //as long as the user has permissions this part will be called
//                        //wether it's the first time he get the permissions or not!!
//                        buildLocationRequest();
//                        buildLocationCallback();
//                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
//                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                                .findFragmentById(R.id.map_mapFragment);
//                        mapFragment.getMapAsync(MapActivity.this);
//
//
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse response) {
//                        Toast.makeText(MapActivity.this, "you must enable permission!", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
//
//                    }
//
//
//                }).check();

        //ui elements
        //map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_mapFragment);
        mapFragment.getMapAsync(this);
        drawerLayout = findViewById(R.id.drawer_layout);

//        mBtnSafe = findViewById(R.id.map_safe_btn);
        mBtnUnsafe = findViewById(R.id.map_unsafe_btn);
        profilePic = findViewById(R.id.map_profile_picture);
        mBtnAddGeofences = findViewById(R.id.add_geo_fences_btn);
        selectedStationTextView = findViewById(R.id.map_station_name_textview);
        currentGeoStateTextView = findViewById(R.id.map_current_geostate);
        feedBtn = findViewById(R.id.feed);
        unsafeVotesTextView = findViewById(R.id.textView_unsafe);
        unsafeVotesTextView.setText("--");

        disableButtons();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
//                Toast.makeText(MapActivity.this, "should open navigation drawer ;)", Toast.LENGTH_SHORT).show();
                if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.openDrawer(Gravity.START);
                else drawerLayout.closeDrawer(Gravity.END);
            }
        });

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(MapActivity.this, "clicked feedBtn", Toast.LENGTH_SHORT).show();

//                intent.putExtra("USER_ID", mAuth.getCurrentUser().getUid());
//                intent.putExtra("STATION", selectedStation);
                Intent intent = new Intent(MapActivity.this, FeedActivity.class);
                intent.putExtra("CURRENT_CITY", currentCity);
                startActivity(intent);
                /**
                 * TODO: need to add a functionality to unlock the feed with a rewarded ad
                 * currently the rewarded ad is disabled
                 */
                if (rewardedAdEnabled) {
                    if (rewardedAd.isLoaded() && !rewardedAdShown) {
                        final Activity activityContext = MapActivity.this;
                        RewardedAdCallback adCallback = new RewardedAdCallback() {
                            @Override
                            public void onRewardedAdOpened() {
                                // Ad opened.
//                            Toast.makeText(activityContext, "onRewardedAdOpened", Toast.LENGTH_SHORT).show();
                                rewardedAd = createAndLoadRewardedAd();
                            }

                            @Override
                            public void onRewardedAdClosed() {
//                            Toast.makeText(activityContext, "onRewardedAdClosed", Toast.LENGTH_SHORT).show();
//                            rewardedAd = createAndLoadRewardedAd();                            // Ad closed.
                            }

                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem reward) {
//                            Toast.makeText(activityContext, "onUserEarnedReward", Toast.LENGTH_SHORT).show();
                                rewardedAd = createAndLoadRewardedAd();                            // User earned reward.
                                rewardedAdShown = true;
                            }

                            @Override
                            public void onRewardedAdFailedToShow(int errorCode) {
//                            Toast.makeText(activityContext, "onRewardedAdFailedToShow", Toast.LENGTH_SHORT).show();
                                rewardedAd = createAndLoadRewardedAd();                            // Ad failed to display.
                            }
                        };
                        rewardedAd.show(activityContext, adCallback);
                    } else {
                        Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                        Toast.makeText(MapActivity.this, "please wait for ad to load!", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, "AdNotLoadedYet", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }


            }
        });

        //clicking initialisation and correction
        safeBtnClicked = false;
        unsafeBtnClicked = false;
        safeVotes = 0;
        unsafeVotes = 0;

        //firebase admin
        stationsNames = new HashMap<String, Object>();
        db = FirebaseFirestore.getInstance();
        //setting the persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //BTN TO ADD GEOFENCES
//        mBtnAddGeofences.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    populateGeofenceList();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                addGeofences();
//            }
//        });

//        mBtnSafe.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //load an interstitial
//                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
//                } else {
//                    Log.d("TAG", "The interstitial wasn't loaded yet.");
//                }
//                safeBtnClicked = true;
//                //old way of updating number of votes
//                DocumentReference currentStationRef;
//                if (selectedStation == null) {
//                    Toast.makeText(MapActivity.this, "Selectionnez une station svp!", Toast.LENGTH_SHORT).show();
//                }
//                if (selectedStation != null) {
//                    currentStationRef =
//                            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(selectedStation);
//                    //                currentStationRef.update("safe votes", FieldValue.increment(1));
////
////                //just in case this is actually a correction of a previous vote
////                //meaning the user has already voted unsafe and is now correcting to safe
////                if (unsafeBtnClicked == true) {
////                    currentStationRef =
////                            db.collection("cities").document("lille").collection("lille stations").document(selectedStation);
////                    currentStationRef.update("unsafe votes", FieldValue.increment(-1));
////                }
//                    //add the user to the current list of SAFE voters and remove it from unsafe voters
//                    //Map to add user to array
//                    final Map<String, Object> addUserToArrayMap = new HashMap<>();
//                    addUserToArrayMap.put("safe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
//
//                    //Map to remove user from array
//                    final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
//                    removeUserFromArrayMap.put("unsafe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
//
//                    currentStationRef.update(addUserToArrayMap);
//                    currentStationRef.update(removeUserFromArrayMap);
//
//                    //query the number of voters and update it
//                    final DocumentReference finalCurrentStationRef = currentStationRef;
//                    currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                            if (e != null) {
//                                Log.w(TAG, "Listen failed.", e);
//                                return;
//                            }
//
//                            if (documentSnapshot != null && documentSnapshot.exists()) {
//                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                                List<String> safeVotersList = (List<String>) documentSnapshot.get("safe voters");
//                                List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                                int safeVoters = safeVotersList.size();
//                                int unsafeVotes = unsafeVotersList.size();
////                          Toast.makeText(MapActivity.this, "number of safe voters" + (safeVotersList.size() - 1), Toast.LENGTH_SHORT).show();
//                                finalCurrentStationRef.update("safe votes", safeVoters);
//                                finalCurrentStationRef.update("unsafe votes", unsafeVotes);
//                            } else {
//                                Log.d(TAG, "Current data: null");
//                            }
//                        }
//                    });
//                }
//
//                /**
//                 * todo: need to add this FEED part
//                 */
//                String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                Map<String, Object> vote = new HashMap<>();
//                vote.put("safe", true);
//                vote.put("station", selectedStation);
//                vote.put("timestamp", Timestamp.now());
//                vote.put("userId", currentuser);
//                if (selectedStation != null) {
//                    db.collection("/cities/" + currentCity + "/" + currentCity + " feed")
//                            .document(currentuser + selectedStation)
//                            .set(vote)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//
//                                }
//                            });
//                }
//
//
//                //edit the ui
////                mBtnSafe.setClickable(false);
////                mBtnUnsafe.setClickable(true);
//            }
//        });

        mBtnUnsafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * before going in to actual functionality we will show an interstitial ad
                 * then we will check if the user's has indeed selected a marker before voting
                 * TODO: hide the voting button outside the screen untill the user selects a marker
                 * when user clicks the button first chek whether the user has already voted on this one before
                 * if he didn't when the + btn is pressed we will add the user's id to the unsafeVoters array
                 * then will will calculate the number of unsafe voters and update it in the "unsafe votes" document
                 * if he did already vote on this marker we will remove the user's id from the unsafeVoters array when - btn is clicked
                 * then we will calculate the number of unsafe voters and update it in the "unsafe votes" document in 'unsafe votes' field
                 * all of this is without using snapshots because snapshots are already used in the onMarkerClick() method
                 * to update the voters array and the number of votes
                 */
                //load an interstitial
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                //in case no marker was selected show a toast message
                if (selectedStation == null) {
                    Toast.makeText(MapActivity.this, "Selectionnez une station svp!", Toast.LENGTH_SHORT).show();
                }

                if (selectedStation != null) {
                    //db
                    DocumentReference currentStationRef;
                    final DocumentReference unsafeVotesReference;
                    final DocumentReference unsafeVotersReference;

                    currentStationRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
                            .document(selectedStation);
                    unsafeVotesReference = currentStationRef.collection("unsafe votes").document("unsafe votes");
                    unsafeVotersReference = currentStationRef.collection("unsafe voters").document("unsafe voters");
                    //old way of updating the number of votes
//                currentStationRef.update("unsafe votes", FieldValue.increment(1));
//
//                //in case of a correction
//                if (safeBtnClicked == true) {
//                    currentStationRef =
//                            db.collection("cities").document("lille").collection("lille stations").document(selectedStation);
//                    currentStationRef.update("safe votes", FieldValue.increment(-1));
//                }

                    if (!unsafeBtnClicked) {
                        unsafeBtnClicked = !unsafeBtnClicked;
                        mBtnUnsafe.setText("-");
                        int newVoteValue;
                        if (unsafeVotesTextView.getText().toString() == "--") {
                            newVoteValue = 1;
                        } else {
                            newVoteValue = Integer.parseInt((unsafeVotesTextView.getText().toString())) + 1;
                        }

                        unsafeVotesTextView.setText(String.valueOf(newVoteValue));
                        //snapshot is used instead of local change
//                        mBtnUnsafe.setText("-");
                        //add the user to the current list of UNSAFE voters and remove it from SAFE voters
                        //Map to add user to array
                        final Map<String, Object> addUserToArrayMap = new HashMap<>();
                        addUserToArrayMap.put("unsafe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

                        //Map to remove user from array
//                        final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
//                        removeUserFromArrayMap.put("safe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));

//                        currentStationRef.update(addUserToArrayMap);
                        unsafeVotersReference.update(addUserToArrayMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: user added to list of unsafe voters!");
//                                Toast.makeText(MapActivity.this, "user added to list of unsafe voters!", Toast.LENGTH_SHORT).show();
                                unsafeVotersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot unsafeVotersDocument = task.getResult();
                                            List<String> unsafeVotersList = (List<String>) unsafeVotersDocument.get("unsafe voters");
                                            if (unsafeVotersList != null) {
                                                unsafeVotesReference.update("unsafe votes", unsafeVotersList.size());
                                            } else {
                                                unsafeVotesReference.update("unsafe votes", 0);
                                            }
                                            enableButtons();
                                        } else {
//                                            Toast.makeText(MapActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: couldn't add user to the list of unsafe voters: " + e.getMessage());
                                Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
//                        currentStationRef.update(removeUserFromArrayMap);


                        //old snapshot
//                        unsafeVotersRegistration = unsafeVotersReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                                    List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                                    int unsafeVotes = unsafeVotersList.size();
//                                    unsafeVotesReference.update("unsafe votes", unsafeVotes);
//                                } else {
//                                    Log.d(TAG, "Current data: null");
//                                }
//                            }
//                        });

                        //query the number of voters and update it
//                        final DocumentReference finalCurrentStationRef = currentStationRef;


                        //old snapshot
//                        currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                if (e != null) {
//                                    Log.w(TAG, "Listen failed.", e);
//                                    return;
//                                }
//
//                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                                    List<String> safeVotersList = (List<String>) documentSnapshot.get("safe voters");
//                                    List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                                    int safeVoters = safeVotersList.size();
//                                    int unsafeVotes = unsafeVotersList.size();
////                                    unsafeVotesTextView.setText(String.valueOf((int) unsafeVotes));
//                                    finalCurrentStationRef.update("safe votes", safeVoters);
//                                    finalCurrentStationRef.update("unsafe votes", unsafeVotes);
//                                } else {
//                                    Log.d(TAG, "Current data: null");
//                                }
//                            }
//                        });

                        /**
                         * todo: need to add this FEED part
                         */
                        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Map<String, Object> vote = new HashMap<>();
                        vote.put("safe", false);
                        vote.put("station", selectedStation);
                        vote.put("timestamp", Timestamp.now());
                        vote.put("userId", currentuser);
                        if ((boolean) userSettingsMap.get("show name")) {
                            vote.put("userName", currentUser.getDisplayName());
                        } else {
                            vote.put("userName", "anon");
                        }
                        if ((boolean) userSettingsMap.get("show profile pic")) {
                            vote.put("profilPicUri", currentUser.getPhotoUrl().toString());
                        } else {
                            vote.put("profilPicUri",
                                    "https://firebasestorage.googleapis.com/v0/b/thisisit-3fcc8.appspot.com/o/art%2Fdefault_avatar.png?alt=media&token=4c44ed1e-9aaf-4db2-b36e-15cc8eb40c66");
                        }

                        if (selectedStation != null) {
                            db.collection("/cities/" + currentCity + "/" + currentCity + " feed")
                                    .document(currentuser + "_" + selectedStation)
                                    .set(vote)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    } else if (unsafeBtnClicked) {
                        unsafeBtnClicked = !unsafeBtnClicked;
                        mBtnUnsafe.setText("+");
                        int newVoteValue = Integer.parseInt((unsafeVotesTextView.getText().toString())) - 1;
                        unsafeVotesTextView.setText(String.valueOf(newVoteValue));
                        /**
                         * trying to copy from the above code and modify it
                         */
                        final Map<String, Object> addUserToArrayMap = new HashMap<>();
                        addUserToArrayMap.put("unsafe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

                        //Map to remove user from array
                        final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
                        removeUserFromArrayMap.put("unsafe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));

//                        currentStationRef.update(removeUserFromArrayMap);
                        unsafeVotersReference.update(removeUserFromArrayMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                unsafeVotersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot unsafeVotersDocument = task.getResult();
                                            List<String> unsafeVotersList = (List<String>) unsafeVotersDocument.get("unsafe voters");
                                            if (unsafeVotersList != null) {
                                                unsafeVotesReference.update("unsafe votes", unsafeVotersList.size());
                                            } else {
                                                unsafeVotesReference.update("unsafe votes", 0);
                                            }
                                            enableButtons();
                                        } else {

//                                            Toast.makeText(MapActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failed to remove user from list of unsafe voters!");

                            }
                        });

                        //query the number of voters and update it
//                        final DocumentReference finalCurrentStationRef = currentStationRef;


                        /**
                         * if the above code works just delete what's down here
                         */
//                        mBtnUnsafe.setText("+");
                        //add the user to the current list of UNSAFE voters and remove it from SAFE voters
                        //Map to add user to array
//                        final Map<String, Object> addUserToArrayMap = new HashMap<>();
//                        addUserToArrayMap.put("safe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));
//
//                        //Map to remove user from array
//                        final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
//                        removeUserFromArrayMap.put("unsafe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));
//
//                        currentStationRef.update(addUserToArrayMap);
//                        currentStationRef.update(removeUserFromArrayMap);
//
//                        //query the number of voters and update it
//                        final DocumentReference finalCurrentStationRef = currentStationRef;
//                        currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                                if (e != null) {
//                                    Log.w(TAG, "Listen failed.", e);
//                                    return;
//                                }
//
//                                if (documentSnapshot != null && documentSnapshot.exists()) {
//                                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                                    List<String> safeVotersList = (List<String>) documentSnapshot.get("safe voters");
//                                    List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                                    int safeVoters = safeVotersList.size();
//                                    int unsafeVotes = unsafeVotersList.size();
//                                    finalCurrentStationRef.update("safe votes", safeVoters);
//                                    finalCurrentStationRef.update("unsafe votes", unsafeVotes);
//                                } else {
//                                    Log.d(TAG, "Current data: null");
//                                }
//                            }
//                        });
                        /**
                         * todo: need to add this FEED part
                         */
                        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Map<String, Object> vote = new HashMap<>();
                        vote.put("safe", false);
                        vote.put("station", selectedStation);
                        vote.put("timestamp", Timestamp.now());
                        vote.put("userId", currentuser);
                        if (selectedStation != null) {
                            db.collection("/cities/" + currentCity + "/" + currentCity + " feed")
                                    .document(currentuser + "_" + selectedStation)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    }
                }
                //snapshot is used instead of local change
//                unsafeBtnClicked = !unsafeBtnClicked;
            }
        });

//        disableUi();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        //ui on click listeners

    }

    private void getUserDetails() {
//        @SuppressLint("RestrictedApi") User myUserDetails = new com.google.firebase.firestore.auth.User();
//        myUserDetails.name = mAuth.getCurrentUser().getDisplayName();
//        myUserDetails.email = mAuth.getCurrentUser().getEmail();
//        myUserDetails.
//
//                String photoUrl = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
    }

    private String photoUrl;

    private void getProfilePic() {
        if (currentUser != null) {
            // find the Facebook profile and get the user's id
            for (UserInfo profile : currentUser.getProviderData()) {
                // check if the provider id matches "facebook.com"
                if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    facebookUserId = profile.getUid();
                    photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                    db.collection("users").document(currentUser.getUid()).update("profiel pic", photoUrl);
                    Picasso.get().load(photoUrl).into(profilePic);
                    Log.d(TAG, "getProfilePic: " + profile.getUid() + " " + photoUrl);
                }
                if (GoogleAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    Picasso.get().load(currentUser.getPhotoUrl()).into(profilePic);
                    Log.d(TAG, "getProfilePic: google: " + profile.getDisplayName() + profile.getPhotoUrl());
                }
            }

        } else {
            Log.d(TAG, "getProfilePic: currentUser is null");
        }
    }

    private void getUserSettings() {
        if (currentuser != null && currentUser.getUid() != null) {
            db.collection("users").document(currentUser.getUid()).collection("settings").document("settings").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            userSettingsMap = documentSnapshot.getData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapActivity.this, "failed to get userSettings! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called!");
//        requestPermissionsWithDexter();

        //admob interstitial ad listener
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        getProfilePic();

        if (currentUser == null) {
//            Toast.makeText(MapActivity.this, "sending to start because the current user i null, onStart()!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onStart: currentUser == null");
            sendToStart();
        }

        //ui
        mToolbar = findViewById(R.id.map_toolbar);
        if (selectedStation != null) {
            Log.d(TAG, "onStart: selecteedStation != null");
//            mToolbar.setTitle(currentCity + ": " + selectedStation);
            mToolbar.setTitle(selectedStation);
        } else {
            Log.d(TAG, "onStart: selectedStation == null");
            mToolbar.setTitle(currentCity);
        }

        setSupportActionBar(mToolbar);
        mBtnChat = findViewById(R.id.map_chat_btn);
        mBtnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(MapActivity.this, ChatActivity.class);
                startActivity(chatIntent);
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called!");
        super.onResume();
    }

    //marker click functionality: get the current station's name and the current votes
    //show an info window


    @Override
    public boolean onMarkerClick(final Marker marker) {
        /**
         * when marker clicked appbar title is updated to the marker's title
         * a snapshot to the votes is added
         * on the same snapshot update whether the current user has voted on this station on not
         * added snapshot detachement
         *
         */
        disableButtons();
        //detaching snapshotListeners
        if (unsafeVotersRegistration != null) {
            unsafeVotersRegistration.remove();
        }
        if (unsafeVotesRegistration != null) {
            unsafeVotesRegistration.remove();
        }

        selectedStation = marker.getTitle();
        if (selectedStation != null) {
//            mToolbar.setTitle(currentCity + ": " + selectedStation);
            mToolbar.setTitle(selectedStation);
            setSupportActionBar(mToolbar);
        }
        DocumentReference unsafeVotesRef;
        DocumentReference unsafeVotersRef;
        if (selectedStation != null) {
            unsafeVotersRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
                    .document(selectedStation).collection("unsafe voters").document("unsafe voters");
            unsafeVotersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot unsafeVotersDocument = task.getResult();
                        List<String> unsafeVotersList = (List<String>) unsafeVotersDocument.get("unsafe voters");
//                        Toast.makeText(MapActivity.this, (CharSequence) unsafeVotersDocument.get("unsafe voters"), Toast.LENGTH_SHORT).show();
                        if (unsafeVotersList != null) {
                            if (unsafeVotersList.contains(mAuth.getCurrentUser().getUid())) {
                                mBtnUnsafe.setText("-");
                                unsafeBtnClicked = true;
                                enableButtons();
                            } else {
                                mBtnUnsafe.setText("+");
                                unsafeBtnClicked = false;
                                enableButtons();
                            }
                        } else {
                            mBtnUnsafe.setText("+");
                            unsafeBtnClicked = false;
                            enableButtons();
                        }

                    } else {
                        Toast.makeText(MapActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        disableButtonsNotConnected();
                    }
                }
            });

            unsafeVotesRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
                    .document(selectedStation).collection("unsafe votes").document("unsafe votes");
            unsafeVotesRegistration = unsafeVotesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                    Toast.makeText(MapActivity.this, "event on unsavfeVOTESregistration", Toast.LENGTH_SHORT).show();
                    if (e == null && documentSnapshot.exists() && documentSnapshot != null) {
                        String unsafeVotesNumber;
                        unsafeVotesNumber = String.valueOf(documentSnapshot.get("unsafe votes"));
                        unsafeVotesTextView.setText(unsafeVotesNumber);
                    } else if (e != null) {
                        Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //unecessary snapshot
//            unsafeVotersRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
//                    .document(selectedStation).collection("unsafe voters").document("unsafe voters");
//            unsafeVotersRegistration = unsafeVotersRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
////                    Toast.makeText(MapActivity.this, "event on unsafeVOTERSregistration", Toast.LENGTH_SHORT).show();
//                    if (e == null && documentSnapshot.exists() && documentSnapshot != null) {
//                        List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                        for (int i = 0; i < unsafeVotersList.size(); i++) {
//                            if (unsafeVotersList.contains(currentUser.getUid())) {
//                                mBtnUnsafe.setText("-");
//                                unsafeBtnClicked = true;
//                                Toast.makeText(MapActivity.this, currentUser.getUid() + " exists!", Toast.LENGTH_SHORT).show();
//                            }
//                            if (!unsafeVotersList.contains(currentUser.getUid())) {
//                                mBtnUnsafe.setText("+");
//                                unsafeBtnClicked = false;
//                                Toast.makeText(MapActivity.this, currentUser.getUid() + " does not exists!", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    } else {
//                        Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            });

            //b4 using the snapshotlistener on a subcollection's document!!!!!!!
            //get the number of unsafe vote & the if the user voted for this station or not
//            registration = unsafeVotesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                    Toast.makeText(MapActivity.this, "EVENT!!!", Toast.LENGTH_SHORT).show();
//                    if (documentSnapshot.exists()) {
//                        List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                        unsafeVotesTextView.setText(String.valueOf((int) unsafeVotersList.size()));
//                        for (int i = 0; i < unsafeVotersList.size(); i++) {
//                            if (unsafeVotersList.contains(currentUser.getUid())) {
//                                mBtnUnsafe.setText("-");
//                                unsafeBtnClicked = true;
//                                Toast.makeText(MapActivity.this, currentUser.getUid() + " exists!", Toast.LENGTH_SHORT).show();
//                            } else if (!unsafeVotersList.contains(currentUser.getUid())) {
//                                mBtnUnsafe.setText("+");
//                                unsafeBtnClicked = false;
//                                Toast.makeText(MapActivity.this, currentUser.getUid() + " does not exists!", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//                }
//            });
            //unused Snapshot
//            currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                    if (e != null) {
//                        Log.w(TAG, "Listen failed.", e);
//                        return;
//                    }
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        Log.d(TAG, "Current data: " + documentSnapshot.getData());
//                        List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
//                        int unsafeVotes = unsafeVotersList.size();
//                        unsafeVotesTextView.setText(String.valueOf((int) unsafeVotes));
//                    } else {
//                        Log.d(TAG, "Current data: null");
//                    }
//                }
//            });
        }

        Log.d(TAG, "onMarkerClick: called!");
//        String markerTitle = marker.getTitle();
//        Toast.makeText(this, "" + markerTitle, Toast.LENGTH_SHORT).show();
        //show an add
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
        if (selectedStation != null) {
            Log.d(TAG, "onMarkerClick: selectedStation != null");
//            mToolbar.setTitle(currentCity + ": " + selectedStation);
            mToolbar.setTitle(selectedStation);
            setSupportActionBar(mToolbar);
        }

        //unused firebaseRef.get()
//        db.collection("cities").document(currentCity).collection(currentCity + " stations").whereEqualTo("location", marker.getPosition())
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            selectedStation = document.getId();
//                            DocumentReference currentStationRef = db.collection("cities").document(currentCity).collection(currentCity + " stations").document(selectedStation);
//                            currentStationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    long safeVotes = 0;
//                                    long unsafeVotes = 0;
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//                                        if (document.exists()) {
//                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
////                                            Toast.makeText(MapActivity.this, "" + document.getData(), Toast.LENGTH_SHORT).show();
//                                            Map<String, Object> stationHashmap = document.getData();
//                                            safeVotes = (long) stationHashmap.get("safe votes");
//                                            unsafeVotes = (long) stationHashmap.get("unsafe votes");
//
////                                            unsafeVotesTextView.setText(String.valueOf((int) unsafeVotes));
////                                            Toast.makeText(MapActivity.this, String.format(getString(R.string.unsafe), getString(R.string.safe), safeVotes, unsafeVotes), Toast.LENGTH_SHORT).show();
//                                            marker.setTag(stationHashmap);
//                                            if (selectedStation != null) {
//                                                mToolbar.setTitle(currentCity + ": " + selectedStation);
//                                                setSupportActionBar(mToolbar);
//                                            }
//
//
//                                        } else {
//                                            Log.d(TAG, "No such document");
//                                        }
//                                    } else {
//                                        Log.d(TAG, "get failed with ", task.getException());
//                                    }
//                                }
//                            });
//                        }
//
////                        Toast.makeText(MapActivity.this, "SELECTED: " + selectedStation, Toast.LENGTH_SHORT).show();
////                        selectedStationTextView.setText(selectedStation);
//                    }
//                });
        return false;
    }

    private void disableButtons() {
        mBtnUnsafe.setClickable(false);
        mBtnUnsafe.setBackgroundResource(R.drawable.btn_round_disabled);
        mBtnUnsafe.setText(".");
        unsafeVotesTextView.setBackgroundResource(R.drawable.edit_round_disabled);
        unsafeVotesTextView.setText("--");
    }

    private void enableButtons() {
        mBtnUnsafe.setClickable(true);
        mBtnUnsafe.setBackgroundResource(R.drawable.btn_round);
        unsafeVotesTextView.setBackgroundResource(R.drawable.edit_round);
    }

    private void disableButtonsNotConnected() {
        mBtnUnsafe.setClickable(true);
        mBtnUnsafe.setBackgroundResource(R.drawable.btn_round);
        mBtnUnsafe.setText("+");
        unsafeVotesTextView.setBackgroundResource(R.drawable.edit_round_disabled);
        unsafeBtnClicked = false;
    }

    /**
     * enable or disable the safe and notSafe btns
     */
    public void enableUi() {
        Log.d(TAG, "enableUi: called!");
//        mBtnSafe.setClickable(true);
        mBtnUnsafe.setClickable(true);
    }

    public void disableUi() {
        Log.d(TAG, "disableUi: called!");
//        mBtnSafe.setClickable(false);
        mBtnUnsafe.setClickable(false);
    }

    /**
     * end of UI enabling methods
     * <p>
     * /////FUCKING PERMISSION SHIT
     * <p>
     * /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        Log.d(TAG, "checkPermissions: called!");
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

//    private void requestPermissions() {
//        Log.d(TAG, "requestPermissions: ");
//        boolean shouldProvideRationale =
//                ActivityCompat.shouldShowRequestPermissionRationale(this,
//                        Manifest.permission.ACCESS_FINE_LOCATION);
//
//        // Provide an additional rationale to the user. This would happen if the user denied the
//        // request previously, but didn't check the "Don't ask again" checkbox.
//        if (shouldProvideRationale) {
//            Log.i(TAG, "Displaying permission rationale to provide additional context.");
//            showSnackbar(R.string.permission_rationale, android.R.string.ok,
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // Request permission
//                            ActivityCompat.requestPermissions(MapActivity.this,
//                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                    REQUEST_PERMISSIONS_REQUEST_CODE);
//                        }
//                    });
//        } else {
//            Log.i(TAG, "Requesting permission");
//            // Request permission. It's possible this can be auto answered if device policy
//            // sets the permission in a given state or the user denied the permission
//            // previously and checked "Never ask again".
//            ActivityCompat.requestPermissions(MapActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_PERMISSIONS_REQUEST_CODE);
//        }
//    }
//
//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult: ");
//        Log.i(TAG, "onRequestPermissionResult");
//        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length <= 0) {
//                // If user interaction was interrupted, the permission request is cancelled and you
//                // receive empty arrays.
//                Log.i(TAG, "User interaction was cancelled.");
//            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.i(TAG, "Permission granted.");
//            } else {
//                // Permission denied.
//
//                // Notify the user via a SnackBar that they have rejected a core permission for the
//                // app, which makes the Activity useless. In a real app, core permissions would
//                // typically be best requested during a welcome-screen flow.
//
//                // Additionally, it is important to remember that a permission might have been
//                // rejected without asking the user for permission (device policy or "Never ask
//                // again" prompts). Therefore, a user interface affordance is typically implemented
//                // when permissions are denied. Otherwise, your app could appear unresponsive to
//                // touches or interactions which have required permissions.
//                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                // Build intent that displays the App settings screen.
//                                Intent intent = new Intent();
//                                intent.setAction(
//                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package",
//                                        BuildConfig.APPLICATION_ID, null);
//                                intent.setData(uri);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }
//                        });
//            }
//        }
//    }


    ///END OF THE FUCKIGN SHIT PERMISSIONS


    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Log.d(TAG, "showSnackbar: called!");
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    /**
     * the on map ready callback method
     *
     * @param googleMap
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called!");
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        updateLocationUI();

        if (mapStyleDark) {
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                this, R.raw.style_json));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }

        }


        if (checkPermissions()) {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //by default mylocaitonbuttonenabled settings
            //old
            //mMap.getUiSettings().setMyLocationButtonEnabled(true);
            View mMapView = mapFragment.getView();
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 330, 180, 0);
            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
            // Turn on the My Location layer and the related control on the map.
        } else {
            requestPermissionsWithDexter();
        }
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

//        addGeoFencing();
    }

    /**
     * add the markers and the geofences in one function
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addGeoFencing() {
        Log.d(TAG, "addGeoFencing: called!");
        Log.d(TAG, "addGeoFencing: after calling getDeviceLocation!");
        JSONObject featuresObject = new JSONObject();
        JSONObject stationObject = new JSONObject();
        int currentCityFile = 0;
        /**
         * TODO: REMOVE THIS SHIT XD
         * I KNOW THIS PART IS ABSOLUTE SHIT
         * BUT THIS IS JUST TEMPORARY TO TRY AND ADD OTHER FEATURES
         * IN THE MEAN TIME WE'LL USE THIS TO:
         *
         */
        if (currentCity == "lille") {
            currentCityFile = R.raw.lille_stations;
        } else if (currentCity == "paris") {
            currentCityFile = R.raw.paris_stations;
        } else if (currentCity == "toulouse") {
            currentCityFile = R.raw.toulouse_stations;
        } else if (currentCity == "lyon") {
            currentCityFile = R.raw.lyon_stations;
        }
        InputStream XmlFileInputStream = getResources().openRawResource(currentCityFile); // getting XML
        String stationsObjectString = readTextFile(XmlFileInputStream);
        try {
            featuresObject = new JSONObject(stationsObjectString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray featuresArray = null;
        try {
            featuresArray = new JSONArray(featuresObject.get("features").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**the new updating functin with location
         * this worked from the first try XD lol
         * TODO: PUT THIS CODE IN THE ADMIN APP THAT I'LL MAKE LATER
         */
//        for (String key : stationsNames.keySet()) {
//            Map<String, Object> city = new HashMap<>();
//            city.put("name", key);
//            city.put("current users", 0);
//            city.put("has internet", true);
//            city.put("line", Arrays.asList("1"));
//            city.put("number of lines", 0);
//            city.put("safe votes", 0);
//            city.put("unsafe votes", 0);
//            city.put("location", stationsNames.get(key));
//            city.put("safe voters", Arrays.asList());
//            city.put("unsafe voters", Arrays.asList());
//
//            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(key)
//                    .set(city)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "onSuccess: added successfully: ");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG, "onFailure: " + e.getMessage());
//                        }
//                    });

//            Map<String, Object> unsafeVotesMap = new HashMap<>();
////            unsafeVotesMap.put("unsafe votes", 0);
////
////            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(key)
////                    .collection("unsafe votes").document("unsafe votes")
////                    .set(unsafeVotesMap)
////                    .addOnFailureListener(new OnFailureListener() {
////                        @Override
////                        public void onFailure(@NonNull Exception e) {
////                            Log.d(TAG, "onFailure: " + e.getMessage());
////                            Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
////                        }
////                    });
////
////            Map<String, Object> unsafeVotersMap = new HashMap<>();
////            unsafeVotersMap.put("unsafe voters", Arrays.asList());
////            unsafeVotersMap.put("test", 1);
////            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(key)
////                    .collection("unsafe voters").document("unsafe voters")
////                    .set(unsafeVotersMap)
////                    .addOnFailureListener(new OnFailureListener() {
////                        @Override
////                        public void onFailure(@NonNull Exception e) {
////                            Log.d(TAG, "onFailure: " + e.getMessage());
////                            Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
////                        }
////                    });
////        }

        /**
         * TODO: I DON'T EVEN KNOW WHAT TEH FUCK AM I DOING RIGHT NOW BROOOOOO!!!
         * THIS IS A FUCKING TEST
         * I'M GOING TO DISABLE ALL THE MARKERS NOW
         * AND I WILL ONLY USE THE MARKERS GIVEN BY THE USERS
         * THIS SHOULD BE BETTER I THINK
         */

        String stationName = "station name";
        String line = "";
        Double latitude = 0.0;
        Double longitude = 0.0;
        LatLng latLng;
        for (int i = 0; i < featuresArray.length(); i++) {
            try {
                stationObject = (JSONObject) featuresArray.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                //get the station's name
                stationName = (String) stationObject.getJSONObject("properties").get("name");
                //get the station's line
                line = (String) stationObject.getJSONObject("properties").get("line");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                //get the station's coordinates
                JSONArray coordinatesArray;
                coordinatesArray = new JSONArray(stationObject.getJSONObject("geometry").getJSONArray("coordinates").toString());
                latitude = (Double) coordinatesArray.get(1);
                longitude = (Double) coordinatesArray.get(0);

                //add the marker
                latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title(stationName));
//                marker.setTag(latLng);
                mMap.setOnMarkerClickListener(this);

                //add the geofence to the geofence list
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
//                        .setRequestId(stationObject.getJSONObject("properties").getString("@id"))
                        .setRequestId("a test")
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                latitude,
                                longitude,
                                100)

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(3000)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build());

                // get the current user's location
                if (latitude != null && longitude != null && currentuser != null) {
                    //geofire stuff
                    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/" + currentuser + "/geofire");
                    geoFire = new GeoFire(ref);
//                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 0.2);//200m
//                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                        @Override
//                        public void onKeyEntered(String key, GeoLocation location) {
//                            LatLng latLng = new LatLng(location.latitude, location.longitude);
////                            Toast.makeText(MapActivity.this, key + " entered " + location, Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "onKeyEntered: " + location);
//                            //todo get the current station name (needs to be fixed 4 fucks sake)
//                            db.collection("cities").document(currentCity).collection(currentCity + " stations").whereEqualTo("location", location)
//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                currentStation = document.getId();
//                                            }
//                                            Toast.makeText(MapActivity.this, "" + currentStation + " at ", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                            selectedStationTextView.setText(currentStation);
//                            enableUi();
//                        }
//
//                        @Override
//                        public void onKeyExited(String key) {
////                            Toast.makeText(MapActivity.this, key + " exited ", Toast.LENGTH_SHORT).show();
////                            disableUi();
//                            safeBtnClicked = false;
//                            unsafeBtnClicked = false;
//                        }
//
//                        @Override
//                        public void onKeyMoved(String key, GeoLocation location) {
////                            Toast.makeText(MapActivity.this, key + " moved ", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onGeoQueryReady() {
////                            Toast.makeText(MapActivity.this, " geoqueryreqdy ", Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "onGeoQueryReady: ");
//                        }
//
//                        @Override
//                        public void onGeoQueryError(DatabaseError error) {
//                            System.err.println("There was an error with this query: " + error);
//                        }
//                    });
                }

//                Log.d(TAG, "addGeoFencing: created on:(" + latitude + "," + longitude + "), for: " + stationObject.getJSONObject("properties").getString("@id"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            stationsNames.put(stationName, new LatLng(latitude, longitude));
        }

        //add the geofences to the geofencing client from the mGeofencesList
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions();
            requestPermissionsWithDexter();
            return;
        }
//        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "onSuccess: geofenccess added!");
////                        Toast.makeText(MapActivity.this, "geo fences added succesfully", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to add geofences! : " + e.getMessage());
////                        Toast.makeText(MapActivity.this, "couldn't add geofences!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });
    }

    /**
     * get the closest marker's name
     */
    public void getClosestMarker() {

    }

    Marker mClosestMarker;
    float mindist;

//    void createMarkersFromJson(String json) throws JSONException {
//        // De-serialize the JSON string into an array of city objects
//        JSONArray jsonArray = new JSONArray(json);
//        for (int i = 0; i < jsonArray.length(); i++) {
//            // Create a marker for each city in the JSON data.
//            JSONObject jsonObj = jsonArray.getJSONObject(i);
//            double lat = jsonObj.getJSONArray("latlng").getDouble(0);
//            double lon = jsonObj.getJSONArray("latlng").getDouble(1);
//            Marker currentMarker = map.addMarker(new MarkerOptions()
//                    .title(jsonObj.getString("name"))
//                    .snippet(Integer.toString(jsonObj.getInt("population")))
//                    .position(new LatLng(
//                            lat,
//                            lon
//                    ))
//            );
//
//            float[] distance = new float[1];
//            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), lat, lon, distance);
//            if (i == 0) {
//                mindist = distance[0];
//            } else if (mindist > distance[0]) {
//                mindist = distance[0];
//                mClosestMarker = currentMarker;
//            }
//        }
//
//        Toast.makeText(MapActivity.this, "Closest Marker Distance: " + mClosestMarker.getTitle() + " " + mindist, Toast.LENGTH_LONG).show();
//    }


    /**
     * a simple function to read from a textFile
     * this function is used to read the geojson in R.raw
     * containing all the stations info
     *
     * @param inputStream
     * @return
     */
    public String readTextFile(InputStream inputStream) {
        Log.d(TAG, "readTextFile: called!");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }


    /**
     * Turn on the My Location layer and the related control on the map.
     */
    private void updateLocationUI() {
        Log.d(TAG, "updateLocationUI: called!");
        if (mMap == null) {
            return;
        }
        try {
            if (checkPermissions()) {
                Log.d(TAG, "updateLocationUI: checkPermissions = true");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                requestPermissionsWithDexter();
                Log.d(TAG, "updateLocationUI: checkPermissions = false");
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
//                requestPermissions();

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    /**
     * a simple function to get the current device's location
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: called!");
//        Toast.makeText(this, "called getDeviceLocation!", Toast.LENGTH_SHORT).show();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if ((checkPermissions() == true) && (currentuser != null)) {
                Log.d(TAG, "getDeviceLocation: checkPermissions && current user != null");
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(MapActivity.this, "onComplete from inside getDeviceLocation!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            mLastKnownLocation = currentLocation;
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                            //geofire stuff
                            currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/" + currentuser + "/geofire");
                            geoFire = new GeoFire(ref);
                            geoFire.setLocation(currentuser, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        Log.d(TAG, "onComplete: error!!" + error);
                                    } else {
                                        Log.d(TAG, "onComplete: geofire location set succefully!");
                                    }
                                    getCurrentCity();
                                }
                            });

                        } else {
                            requestPermissionsWithDexter();
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
            Toast.makeText(MapActivity.this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "getDeviceLocation: ENDED!!");
    }

    /**
     * move the camera to the latlng with a determined zoom
     *
     * @param latLng
     * @param zoom
     */
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to " + latLng.latitude + "," + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
    }

    /**
     * OptionsMenu functions:
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (item.getItemId() == R.id.main_logout_btn) {
            db.collection("users").document(currentUser.getUid()).update("logged in", false)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FirebaseAuth.getInstance().signOut();
                            mAuth.signOut();
                            LoginManager.getInstance().logOut();
                            sendToStart();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                    sendToStart();
                }
            });
        }
//        if (item.getItemId() == R.id.main_account_settings_btn) {
//            Intent settingsIntent = new Intent(MapActivity.this, SettingsActivity.class);
//            startActivity(settingsIntent);
//        }
        if (item.getItemId() == R.id.main_choose_transport_type_btn) {
            Intent settingsIntent = new Intent(MapActivity.this, TransportTypeActivity.class);
            startActivity(settingsIntent);
        }
        if (item.getItemId() == R.id.main_privacy_policy_btn) {
            Intent privacyPolicyIntent = new Intent(MapActivity.this, PrivacyPolicyActivity.class);
            startActivity(privacyPolicyIntent);
        }
        return true;
    }


    /**
     * options Menu functions end
     * <p>
     * /**
     * send to start activity to register or login the comeback
     */
    private void sendToStart() {
        Log.d(TAG, "sendToStart: ");
        Intent startIntent = new Intent(MapActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();//not comeback when back button is pressed
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                Toast.makeText(MapActivity.this, "locationResult got!!", Toast.LENGTH_SHORT).show();
                if (mMap != null) {
                    lastLocation = locationResult.getLastLocation();
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();
                    currentUser = mAuth.getCurrentUser();
                    if (currentUser == null) {
                        Log.d(TAG, "onLocationResult: sending to start because currentUser object == null");
                        Toast.makeText(MapActivity.this, "Please log in to use the app!", Toast.LENGTH_SHORT).show();
                        sendToStart();
                    } else if ((boolean) userSettingsMap.get("get position")) {
                        //update last location in firestore
                        if (lastLocation != null && (boolean) userSettingsMap.get("get position")) {
//                            Map<String, Object> userMap = new HashMap<>();
//                            userMap.put("current location", new GeoPoint(location.getLatitude(), location.getLongitude()));
                            db.collection("users").document(currentUser.getUid())
                                    .update("current location", new GeoPoint(latitude, longitude))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
//                                            Toast.makeText(MapActivity.this, "updated current location", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: could not update location on firestore: " + e.getMessage());
//                                    Toast.makeText(MapActivity.this, "could not update location on db: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                        /**
                         *  TODO: ACTIVATE THIS FEATURE AFTER TRIAL
                         * THIS IS A BETA FEATURE IT WILL BE USED WITH AI TO DETERMINE THE SAFEST ROUTE WITHOUT
                         * HUMAN INTERACTION, ALL BY GETTING THE LOCATION OF EVERY USER AND SAVING IT AT ALL TIMES
                         * TAKING INTO EFFECT THEIR PAST DECISIONS AND IF THEY HAVE A CARD AND THE CHANGES THAT OCCUR
                         * TO THEIR DAILY SCHEDULE.
                         * FOR NOW WE WILL STICK TO SAVING THE LAST KNOWN LOCATION ONLY
                         *
                         *
                         */
                        // create new document for each new location
//                        Map<String, Object> locationMap = new HashMap<>();
//                        locationMap.put("location", new GeoPoint(latitude, longitude));
//                        locationMap.put("id", currentUser.getUid());
//                        locationMap.put("timestamp", Timestamp.now());
//                        String roundedGeolocation;
//                        roundedGeolocation = String.format("(%.02f,%.02f)", latitude, longitude);
//                        db.collection("users").document(currentUser.getUid()).collection("location")
//                                .document(currentUser.getUid() + roundedGeolocation)
//                                .set(locationMap)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Toast.makeText(MapActivity.this, "added document to location collection!", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
                    }
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    /**
     * THESE ARE NOT USED FOR THE MOMENT
     * ALL OF THE CODE AFTER THIS LINE IS FOR FUTURE FEATURES
     * YOU CAN COMMENT ALL OF THIS WITH NO PROBLEM WHATSOEVER
     * IN CASE YOU WANT TO REMOVE THIS CODE DO NOT DELETE JUST COMMENT IT
     *
     * @throws JSONException
     */
    private void populateGeofenceList() throws JSONException {
        //read the geojson file from R.raw
        int currentCityFile = 0;
        if (currentCity == "lille") {
            currentCityFile = R.raw.lille_stations;
        } else if (currentCity == "paris") {
            currentCityFile = R.raw.paris_stations;
        }
        InputStream XmlFileInputStream = getResources().openRawResource(currentCityFile); // getting XML
        String stationsGeojsonString = readTextFile(XmlFileInputStream);

        JSONObject featuresObject = null;
        String stationName = "";
        String line = "";
        double latitude = 0.0;
        double longitude = 0.0;
        String stationId = "";
        //empty the mGeofenceList array list
        mGeofenceList.clear();

        for (int i = 0; i < (new JSONObject(stationsGeojsonString).getJSONArray("features").length()); i++) {
            try {
                featuresObject = (JSONObject) new JSONObject(stationsGeojsonString).getJSONArray("features").get(i);
                //get the station's name
                stationName = (String) featuresObject.getJSONObject("properties").get("name");
                line = (String) featuresObject.getJSONObject("properties").get("line");
                //get the station's latitude and longitude
                latitude = (double) featuresObject.getJSONObject("geometry").getJSONArray("coordinates").get(1);
                longitude = (double) featuresObject.getJSONObject("geometry").getJSONArray("coordinates").get(0);
                //get the station's id
//                stationId = (String) featuresObject.getJSONObject("properties").get("@id");

                //add the geofence to mGeofenceList
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(stationName)
                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                latitude,
                                longitude,
                                200)

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(3000)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build());

                drawCirlce(latitude, longitude, 200, 0);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "populateGeofenceList: mGeofenceList is: " + mGeofenceList);
    }

    private void drawCirlce(double latitude, double longitude, double radius, int mode) {
        Circle circle;
        if (mode == 0) {
            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(radius)
                    .strokeWidth(10)
                    .strokeColor(Color.BLACK)
                    .clickable(true)
                    .fillColor(0x99ffffff)
                    .strokeWidth(5.0f));
        } else if (mode == 1) {

        } else if (mode == 2) {

        }


    }
    /**
     * get the needed location permissions
     * that is fine location only (feeling like adding coarse location later idk XD)
     */
//    private void getLocationPermission() {
//        Log.d(TAG, "getLocationPermission: ");
//        /*
//         * Request location permission, so that we can get the location of the
//         * device. The result of the permission request is handled by a callback,
//         * onRequestPermissionsResult.
//         */
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
//    }

//    /**
//     * show the activity to get the permissions
//     *
//     * @param requestCode
//     * @param permissions
//     * @param grantResults
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        Log.d(TAG, "onRequestPermissionsResult: ");
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                }
//            }
//        }
//        updateLocationUI();
//    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void performPendingGeofenceTask() {
//        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
////            addGeofences();
//        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
//            removeGeofences();
//        }
//    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
//    private void removeGeofences() {
//        if (!checkPermissions()) {
//            showSnackbar(getString(R.string.insufficient_permissions));
//            return;
//        }
//
//        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
//    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
//            setButtonsEnabledState();

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Stores whether geofences were added ore removed in
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }


//    /**
//     * Adds geofences. This method should be called after the user has granted the location
//     * permission.
//     */
//    @SuppressWarnings("MissingPermission")
//    private void addGeofences() {
//        if (geofencesAdded = false) {
//            if (!checkPermissions()) {
//                showSnackbar(getString(R.string.insufficient_permissions));
//                return;
//            }
//
//            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(MapActivity.this, "added geofences successfully", Toast.LENGTH_SHORT).show();
//                            geofencesAdded = true;
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(MapActivity.this, "failed to add geofences: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            geofencesAdded = false;
//                        }
//                    });
//        }
//    }

//    /**
//     * add the geofences to the map
//     * called when the user clicks the add geofences btn
//     * the mGeofenceList should already have been populated
//     */
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public void addGeofences() {
//        Log.d(TAG, "addGeofences: ");
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions();
//            return;
//        }
//        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "onSuccess: geofenccess added!");
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: failed to add geofences! : " + e.getMessage());
//                    }
//                });
//    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        Log.d(TAG, "getGeofencingRequest()");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG, "getGeofencePendingIntent: ");
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     *
     *
     *
     *
     *
     */

    /**
     * THIS PART IS TO RUN IN FUTURE ADMIN APP TO RESTORE VOTES TO 0
     */


    /**
     * add list of stations to the firebase firestore db
     */

//        for (int i = 0; i < stationsNames.size(); i++) {
//            Map<String, Object> city = new HashMap<>();
//            city.put("name", stationsNames.get(i));
//            city.put("current users", 0);
//            city.put("has internet", true);
//            city.put("line names", Arrays.asList("jaune"));
//            city.put("number of lines", 0);
//            city.put("safe votes", 0);
//            city.put("unsafe votes", 0);
//            city.put("location", stationName
//
//            db.collection("cities").document("lille").collection("lille stations").document(stationsNames.get(i))
//                    .set(city)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(TAG, "onSuccess: added successfully: ");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.d(TAG, "onFailure: " + e.getMessage());
//                        }
//                    });
//
//        }
    public void getCurrentCityFromStart() {
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        HashMap<String, LatLng> cityList = new HashMap<>();
                        cityList.put("lille", new LatLng(50.6292, 3.0573));
                        cityList.put("lyon", new LatLng(45.7640, 4.8357));
                        cityList.put("marseille", new LatLng(43.2965, 5.3698));
                        cityList.put("paris", new LatLng(48.8566, 2.3522));
                        cityList.put("rennes", new LatLng(48.1173, 1.6778));
                        cityList.put("toulouse", new LatLng(43.6047, 1.4442));
                        String closestCity = "";
                        if (location != null) {
                            // Logic to handle location object
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            double minimumDistance = 100000000;
                            for (String city : cityList.keySet()) {
                                if (minimumDistance > pow(pow(latitude - cityList.get(city).latitude, 2) + pow(longitude - cityList.get(city).longitude, 2), 0.2)) {
                                    minimumDistance = pow(pow(latitude - cityList.get(city).latitude, 2) + pow(longitude - cityList.get(city).longitude, 2), 0.2);
                                    closestCity = city;
//                                    Toast.makeText(MapActivity.this, "city: " + closestCity + " at a distance of: " + minimumDistance, Toast.LENGTH_SHORT).show();
                                    currentCity = closestCity;
                                }
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                addGeoFencing();
                            }
                        }
//                        Toast.makeText(MapActivity.this, "currentCity is: " + currentCity, Toast.LENGTH_SHORT).show();
                        // since this function is called from onStart we will not zoom in to the current user's location because
                        // each time the user changes activity and comes back it will change the zoom (for example when the user
                        // goes to the feed and clicks the back button he will have to set the camera's position again
                        if (location != null && selectedStation != null) {
//                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
//                                    DEFAULT_ZOOM);
//                            mToolbar.setTitle(currentCity + ": " + selectedStation);
                            mToolbar.setTitle(selectedStation);
                        } else {
                            Toast.makeText(MapActivity.this, "please logout then login again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getCurrentCity() {
//        Toast.makeText(MapActivity.this, "called getCurrentCity!", Toast.LENGTH_SHORT).show();
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        HashMap<String, LatLng> cityList = new HashMap<>();
                        cityList.put("lille", new LatLng(50.6292, 3.0573));
                        cityList.put("lyon", new LatLng(45.7640, 4.8357));
                        cityList.put("marseille", new LatLng(43.2965, 5.3698));
                        cityList.put("paris", new LatLng(48.8566, 2.3522));
                        cityList.put("rennes", new LatLng(48.1173, 1.6778));
                        cityList.put("toulouse", new LatLng(43.6047, 1.4442));
                        String closestCity = "";
                        if (location != null) {
                            // Logic to handle location object
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            double minimumDistance = 100000000;
                            for (String city : cityList.keySet()) {
                                if (minimumDistance > pow(pow(latitude - cityList.get(city).latitude, 2) + pow(longitude - cityList.get(city).longitude, 2), 0.2)) {
                                    minimumDistance = pow(pow(latitude - cityList.get(city).latitude, 2) + pow(longitude - cityList.get(city).longitude, 2), 0.2);
                                    closestCity = city;
//                                    Toast.makeText(MapActivity.this, "city: " + closestCity + " at a distance of: " + minimumDistance, Toast.LENGTH_SHORT).show();
                                    currentCity = closestCity;
                                }
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                addGeoFencing();
                            }
                        }
//                        Toast.makeText(MapActivity.this, "currentCity is: " + currentCity, Toast.LENGTH_SHORT).show();
                        if (location != null) {
                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                                    DEFAULT_ZOOM);
                            mToolbar.setTitle(currentCity);
                        } else {
                            Toast.makeText(MapActivity.this, "please logout then login again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void removeMarkersFromMap() {
        Log.d(TAG, "removeMarkersFromMap: called!");
        if (mMap != null) {
            Log.d(TAG, "removeMarkersFromMap: mMap != null");
            mMap.clear();
        }

    }


    public void requestPermissionsWithDexter() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        //as long as the user has permissions this part will be called
                        //wether it's the first time he get the permissions or not!!
//                        Toast.makeText(MapActivity.this, "the permissions are granted!", Toast.LENGTH_SHORT).show();
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
                        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map_mapFragment);
                        mapFragment.getMapAsync(MapActivity.this);
                        getDeviceLocation();
                        getCurrentCity();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapActivity.this, "you must enable permission!", Toast.LENGTH_SHORT).show();
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

    public RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd = new RewardedAd(this,
                getString(R.string.rewarded_ad_id));
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }
}
