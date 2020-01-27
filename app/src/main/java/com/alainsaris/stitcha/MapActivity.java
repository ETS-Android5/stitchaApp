package com.alainsaris.stitcha;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // keep track of all the markers
    List<Marker> markers = new ArrayList<>();
    HashMap<String, Marker> visibleMarkers = new HashMap<String, Marker>();

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

    // marker creation options
    String markerType;

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

    int drawableId;
    String markerMessage;

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
    private boolean mapClickListenerActivated = false;

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
                String result = data.getStringExtra("type");
                markerMessage = data.getStringExtra("message");
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                addMarker(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void addMarker(String result) {
        drawableId = R.drawable.markers_fire_marker;

        markerType = "alert";
        switch (result) {
            case "policeMarker":
                drawableId = R.drawable.markers_police_marker;
                markerType = "police";
                break;
            case "ticketsMarker":
                drawableId = R.drawable.markers_ticket_marker;
                markerType = "tickets";
                break;
            case "fireMarker":
                drawableId = R.drawable.markers_fire_marker;
                markerType = "fire";
                break;
            case "protestMarker":
                drawableId = R.drawable.markers_protests_marker;
                markerType = "protests";
                break;
        }
        mapClickListenerActivated = true;

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

    @SuppressLint("RestrictedApi")
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

        // GPS SHIT
//        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

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

        //ui elements
        //map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_mapFragment);
        mapFragment.getMapAsync(this);
        drawerLayout = findViewById(R.id.drawer_layout);

//        mBtnSafe = findViewById(R.id.map_safe_btn);
        mBtnUnsafe = findViewById(R.id.map_unsafe_btn);
        mBtnUnsafe.setVisibility(View.GONE);
        profilePic = findViewById(R.id.map_profile_picture);
        profilePic.setClickable(false);
        mBtnAddGeofences = findViewById(R.id.add_geo_fences_btn);
        selectedStationTextView = findViewById(R.id.map_station_name_textview);
        currentGeoStateTextView = findViewById(R.id.map_current_geostate);
        feedBtn = findViewById(R.id.feed);
        unsafeVotesTextView = findViewById(R.id.textView_unsafe);
        unsafeVotesTextView.setVisibility(View.GONE);
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
        profilePic.setClickable(false);

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, FeedActivity.class);
                if (lastLocation != null) {
                    intent.putExtra("LATITUDE", lastLocation.getLatitude());
                    intent.putExtra("LONGITUDE", lastLocation.getLongitude());
                }

                startActivity(intent);
                // This makes the new screen slide up as it fades in
                // while the current screen slides up as it fades out.
                overridePendingTransition(R.anim.up_in, R.anim.up_out);
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
         *
         * this is the new way of voting without adding markers and then voting
         * just putting the markers on the map and they are visible to all the users
         * we do not need the feed or the vote button for the moment
         */

        // we start off by showing an ad
        Log.d(TAG, "onMarkerClick: called!");
        //show an add
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }


        /**
         * OLD SHIT UNDER ->
         * when marker clicked appbar title is updated to the marker's title
         * a snapshot to the votes is added
         * on the same snapshot update whether the current user has voted on this station on not
         * added snapshot detachement
         *
         */
//        disableButtons();
//        //detaching snapshotListeners
//        if (unsafeVotersRegistration != null) {
//            unsafeVotersRegistration.remove();
//        }
//        if (unsafeVotesRegistration != null) {
//            unsafeVotesRegistration.remove();
//        }
//
//        selectedStation = marker.getTitle();
//        if (selectedStation != null) {
////            mToolbar.setTitle(currentCity + ": " + selectedStation);
//            mToolbar.setTitle(selectedStation);
//            setSupportActionBar(mToolbar);
//        }
//        DocumentReference unsafeVotesRef;
//        DocumentReference unsafeVotersRef;
//        if (selectedStation != null) {
//            unsafeVotersRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
//                    .document(selectedStation).collection("unsafe voters").document("unsafe voters");
//            unsafeVotersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot unsafeVotersDocument = task.getResult();
//                        List<String> unsafeVotersList = (List<String>) unsafeVotersDocument.get("unsafe voters");
////                        Toast.makeText(MapActivity.this, (CharSequence) unsafeVotersDocument.get("unsafe voters"), Toast.LENGTH_SHORT).show();
//                        if (unsafeVotersList != null) {
//                            if (unsafeVotersList.contains(mAuth.getCurrentUser().getUid())) {
//                                mBtnUnsafe.setText("-");
//                                unsafeBtnClicked = true;
//                                enableButtons();
//                            } else {
//                                mBtnUnsafe.setText("+");
//                                unsafeBtnClicked = false;
//                                enableButtons();
//                            }
//                        } else {
//                            mBtnUnsafe.setText("+");
//                            unsafeBtnClicked = false;
//                            enableButtons();
//                        }
//
//                    } else {
//                        Toast.makeText(MapActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        disableButtonsNotConnected();
//                    }
//                }
//            });
//
//            unsafeVotesRef = db.collection("cities").document(currentCity).collection(currentCity + " stations")
//                    .document(selectedStation).collection("unsafe votes").document("unsafe votes");
//            unsafeVotesRegistration = unsafeVotesRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
////                    Toast.makeText(MapActivity.this, "event on unsavfeVOTESregistration", Toast.LENGTH_SHORT).show();
//                    if (e == null && documentSnapshot.exists() && documentSnapshot != null) {
//                        String unsafeVotesNumber;
//                        unsafeVotesNumber = String.valueOf(documentSnapshot.get("unsafe votes"));
//                        unsafeVotesTextView.setText(unsafeVotesNumber);
//                    } else if (e != null) {
//                        Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//        if (selectedStation != null) {
//            Log.d(TAG, "onMarkerClick: selectedStation != null");
////            mToolbar.setTitle(currentCity + ": " + selectedStation);
//            mToolbar.setTitle(selectedStation);
//            setSupportActionBar(mToolbar);
//        }
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
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("markers");
        final GeoFirestore geoFirestore = new GeoFirestore(collectionRef);
        Log.d(TAG, "onMapReady: called!");
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mapClickListenerActivated) {

                    //add merker
//                    int height = 100;
//                    int width = 100;
//                    Bitmap b = BitmapFactory.decodeResource(getResources(), drawableId);
//                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
//                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    LatLng markerPosition = new LatLng(latLng.latitude, latLng.longitude);
//                    MarkerOptions markerOptions = new MarkerOptions().position(markerPosition)
//                            .title(markerType + ": " + markerMessage)
//                            .icon(markerIcon);
//                    mMap.addMarker(markerOptions);
                    mapClickListenerActivated = false;


                    /**
                     * PUT THE MARKER IN GEOFIRESTORE
                     * LOAD ALL THE MARKERS THAT ARE
                     * WITHIN THE CURRENT USER'S LOCATION QUERY
                     */
                    // put marker in firestore
                    String docId = markerPosition.latitude + "," + markerPosition.longitude + "_" + currentUser.getUid();
                    try {
                        // first it will create a marker document containing only the geofirestore data
                        // then it will add the other marker data to the document (type, message, id, timestamp)
                        DocumentReference docRef = db.collection("markers").document(docId);
                        Map<String, Object> markerMap = new HashMap<>();
                        markerMap.put("type", markerType);
                        markerMap.put("message", markerMessage);
                        markerMap.put("userId", currentUser.getUid());
                        markerMap.put("timestamp", Timestamp.now().toDate());
                        docRef.set(markerMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                        geoFirestore.setLocation(docId, new GeoPoint(markerPosition.latitude, markerPosition.longitude));
                    } catch (Exception e) {
                        if (e == null) {
                            Log.d(TAG, "Location saved on server successfully!");
                            // here we add other info to the just created doc
                        } else {
                            Log.d(TAG, "couldn't save location on map: " + e.getMessage());
                        }
                    }


                } else {
                    //do nothing for the moment
                }
            }
        });
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

    }

    /**
     * get the closest marker's name
     */
    public void getClosestMarker() {

    }

    Marker mClosestMarker;
    float mindist;

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
        if ((checkPermissions() == true) && (currentuser != null)) {
            Log.d(TAG, "getDeviceLocation: checkPermissions && current user != null");
            Toast.makeText(MapActivity.this, "permission is and currentuser also!", Toast.LENGTH_SHORT).show();
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MapActivity.this, "onComplete from inside getDeviceLocation!", Toast.LENGTH_SHORT).show();
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

                        //geofirestore stuff
                        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("markers");
                        GeoFirestore geoFirestore = new GeoFirestore(collectionRef);
                        GeoQuery geoQuery = geoFirestore.queryAtLocation(
                                new GeoPoint(latitude, longitude),
                                15);// 15 kms bro
                        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                            @Override
                            public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                                if (documentSnapshot.get("type") == null) {
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // Actions to do after 10 seconds

                                        }
                                    }, 100);
                                }
                                String type = (String) documentSnapshot.get("type");
                                String message = (String) documentSnapshot.get("message");
                                Timestamp timestamp = (Timestamp) documentSnapshot.get("timestamp");
                                String time = null;
                                if (timestamp != null) {
                                    time = timestamp.toDate().toString();
                                }

                                int height = 100;
                                int width = 100;
                                int markerDrawableId = R.drawable.markers_police_marker;

                                if (type != null) {
                                    switch (type) {
                                        case "police":
                                            markerDrawableId = R.drawable.markers_police_marker;
                                            break;
                                        case "tickets":
                                            markerDrawableId = R.drawable.markers_ticket_marker;
                                            break;
                                        case "fire":
                                            markerDrawableId = R.drawable.markers_fire_marker;
                                            break;
                                        case "protests":
                                            markerDrawableId = R.drawable.markers_protests_marker;
                                            break;
                                    }
                                }

                                double timeDifference = Timestamp.now().getSeconds() - timestamp.getSeconds();
                                double alphaValue = (100 + timeDifference * -0.00135) / 100;
                                if (alphaValue < 0) {
                                    alphaValue = 0;
                                }
                                boolean markerVisible = true;
                                if (alphaValue == 0) {
                                    markerVisible = false;
                                }

                                Bitmap b = BitmapFactory.decodeResource(getResources(), markerDrawableId);
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                                LatLng markerPosition = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions().position(markerPosition)
                                        .title("Il y a " + secToTime((int) timeDifference) + (message == " " ? "" : ": '" + message + "'"))
                                        .icon(markerIcon)
                                        .visible(markerVisible)
                                        .alpha((float) alphaValue);
                                Log.d(TAG, "onDocumentEntered: added marker: " + alphaValue);
                                Marker marker = mMap.addMarker(markerOptions);

                                try {
                                    visibleMarkers.put(documentSnapshot.getId(), marker);
//                                    Toast.makeText(MapActivity.this, "marker added to visiblemarkers hashmap!", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
//                                Toast.makeText(MapActivity.this, "" + visibleMarkers.keySet(), Toast.LENGTH_SHORT).show();
//                                marker.setTag(documentSnapshot.getId());
//                                mapClickListenerActivated = false;


                            @Override
                            public void onDocumentExited(DocumentSnapshot documentSnapshot) {
//                                Toast.makeText(MapActivity.this, "exited: " + documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                                try {
                                    Marker oldMarker = visibleMarkers.get(documentSnapshot.getId());
                                    oldMarker.remove();
                                } catch (Exception e) {
                                    Log.d(TAG, "onDocumentExited: " + e.getMessage());
                                    Toast.makeText(MapActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                                Toast.makeText(MapActivity.this, "geofirestore: documentMoved", Toast.LENGTH_SHORT).show();
//                                visibleMarkers.get(documentSnapshot.getId()).remove();
                                visibleMarkers.get(documentSnapshot.getId()).setPosition(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                            }

                            @Override
                            public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                                Toast.makeText(MapActivity.this, "geofirestore: document changed", Toast.LENGTH_SHORT).show();
                                Marker oldMarker = visibleMarkers.get(documentSnapshot.getId());
                                if (oldMarker != null) {
                                    oldMarker.remove();
                                    visibleMarkers.remove(documentSnapshot.getId());
                                } else {
                                    Log.d(TAG, "onDocumentChanged: this is a new marker oldMarker == null");
                                }
                                String type = (String) documentSnapshot.get("type");
//                                Toast.makeText(MapActivity.this, type, Toast.LENGTH_SHORT).show();
                                String message = (String) documentSnapshot.get("message");
                                Timestamp timestamp = (Timestamp) documentSnapshot.get("timestamp");
                                String time = null;
                                if (timestamp != null) {
                                    time = timestamp.toDate().toString();
                                }

                                int height = 100;
                                int width = 100;
                                int drawableId = R.drawable.markers_fire_marker;

                                if (type != null) {
                                    switch (type) {
                                        case "police":
                                            drawableId = R.drawable.markers_police_marker;
                                            break;
                                        case "tickets":
                                            drawableId = R.drawable.markers_ticket_marker;
                                            break;
                                        case "fire":
                                            drawableId = R.drawable.markers_fire_marker;
                                            break;
                                        case "protests":
                                            drawableId = R.drawable.markers_protests_marker;
                                            break;
                                    }
                                }

                                double timeDifference = Timestamp.now().getSeconds() - timestamp.getSeconds();
                                double alphaValue = (100 + timeDifference * -0.00135) / 100;
                                if (alphaValue < 0) {
                                    alphaValue = 0;
                                }
                                boolean markerVisible = true;
                                if (alphaValue == 0) {
                                    markerVisible = false;
                                }

                                Bitmap b = BitmapFactory.decodeResource(getResources(), drawableId);
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                                LatLng markerPosition = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions().position(markerPosition)
                                        .title("Il y a " + secToTime((int) timeDifference) + (message == " " ? "" : ": '" + message + "'"))
                                        .icon(markerIcon)
                                        .visible(markerVisible)
                                        .alpha((float) alphaValue);
                                Log.d(TAG, "onDocumentChanged: added marker: " + alphaValue);
                                Marker marker = mMap.addMarker(markerOptions);
                                visibleMarkers.put(documentSnapshot.getId(), marker);

                            }

                            @Override
                            public void onGeoQueryReady() {
//                                Toast.makeText(MapActivity.this, "ready!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onGeoQueryError(Exception e) {
                                Log.d(TAG, "onGeoQueryError: " + e.getMessage());
//                                Toast.makeText(MapActivity.this, "geofirestore: error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


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

    //Note that the type "Items" will be whatever type of object you're adding markers for so you'll
    //likely want to create a List of whatever type of items you're trying to add to the map and edit this appropriately
    //Your "Item" class will need at least a unique id, latitude and longitude.
//    private void addItemsToMap(List<ClipData.Item> items) {
//        if (this.mMap != null) {
//            //This is the current user-viewable region of the map
//            LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;
//
//            //Loop through all the items that are available to be placed on the map
//            for (ClipData.Item item : items) {
//
//                //If the item is within the the bounds of the screen
//                if (bounds.contains(new LatLng(item.getLatitude(), item.getLongitude()))
//                {
//                    //If the item isn't already being displayed
//                    if (!courseMarkers.containsKey(item.getId())) {
//                        //Add the Marker to the Map and keep track of it with the HashMap
//                        //getMarkerForItem just returns a MarkerOptions object
//                        this.courseMarkers.put(item.getId(), this.mMap.addMarker(getMarkerForItem(item)));
//                    }
//                }
//
//                //If the marker is off screen
//            else
//                {
//                    //If the course was previously on screen
//                    if (courseMarkers.containsKey(item.getId())) {
//                        //1. Remove the Marker from the GoogleMap
//                        courseMarkers.get(item.getId()).remove();
//
//                        //2. Remove the reference to the Marker from the HashMap
//                        courseMarkers.remove(item.getId());
//                    }
//                }
//            }
//        }
//    }

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
     * Runs when the result of calling {addGeofences()} and/or {removeGeofences()}
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
                                /**
                                 * TODO: IF THIS SHIT DOESN'T WORK RE-ENABLE IT
                                 */
//                                addGeoFencing();
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

    String secToTime(int sec) {
        int second = sec % 60;
        int minute = sec / 60;
        if (minute >= 60) {
            int hour = minute / 60;
            minute %= 60;
            return hour + "h " + (minute < 10 ? "0" + minute : minute) + "min " + (second < 10 ? "0" + second : second) + "s";
        }
        return minute + "min " + (second < 10 ? "0" + second : second) + "s";
    }

}
