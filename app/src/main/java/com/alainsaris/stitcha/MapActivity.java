package com.alainsaris.stitcha;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.facebook.login.LoginManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.pow;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnCompleteListener<Void>,
        GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MAP_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static Application sApplication;
    boolean rewardedAdShown = false;

    //admob
    private InterstitialAd mInterstitialAd;
    private AdView adView;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //ui
    private Button mBtnChat;
    private Button mBtnSafe;
    private Button mBtnUnsafe;
    private Button mBtnAddGeofences;
    private Toolbar mToolbar;
    private TextView selectedStationTextView;
    private TextView currentStationTextView;
    private TextView currentGeoStateTextView;

    SupportMapFragment mapFragment;

    //map
    //private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private CameraPosition mCameraPosition;
    private static final int DEFAULT_ZOOM = 15;
    private String selectedStation;
    private String currentStation;
    private String currentCity;


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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        requestPermissionsWithDexter();

        //admob
        //banner
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

        mBtnSafe = findViewById(R.id.map_safe_btn);
        mBtnUnsafe = findViewById(R.id.map_unsafe_btn);
        mBtnAddGeofences = findViewById(R.id.add_geo_fences_btn);
        selectedStationTextView = findViewById(R.id.map_station_name_textview);
        currentGeoStateTextView = findViewById(R.id.map_current_geostate);
        FloatingActionButton feedBtn = findViewById(R.id.feed);

        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MapActivity.this, "clicked feedBtn", Toast.LENGTH_SHORT).show();

//                intent.putExtra("USER_ID", mAuth.getCurrentUser().getUid());
//                intent.putExtra("STATION", selectedStation);
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
                            Intent intent = new Intent(MapActivity.this, FeedActivity.class);
                            intent.putExtra("CURRENT_CITY", currentCity);
                            startActivity(intent);
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
                } else if (rewardedAdShown) {
                    Intent intent = new Intent(MapActivity.this, FeedActivity.class);
                    intent.putExtra("CURRENT_CITY", currentCity);
                    startActivity(intent);
                } else {
                    Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                    Toast.makeText(MapActivity.this, "please wait for ad to load!", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, "AdNotLoadedYet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //clicking functionallity and correction
        safeBtnClicked = false;
        unsafeBtnClicked = false;
        safeVotes = 0;
        unsafeVotes = 0;

        //firebase admin
        stationsNames = new HashMap<String, Object>();
        db = FirebaseFirestore.getInstance();

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

        mBtnSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load an interstitial
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                safeBtnClicked = true;
                //old way of updating number of votes
                DocumentReference currentStationRef;
                if (selectedStation == null) {
                    Toast.makeText(MapActivity.this, "Selectionnez une station svp!", Toast.LENGTH_SHORT).show();
                }
                if (selectedStation != null) {
                    currentStationRef =
                            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(selectedStation);
                    //                currentStationRef.update("safe votes", FieldValue.increment(1));
//
//                //just in case this is actually a correction of a previous vote
//                //meaning the user has already voted unsafe and is now correcting to safe
//                if (unsafeBtnClicked == true) {
//                    currentStationRef =
//                            db.collection("cities").document("lille").collection("lille stations").document(selectedStation);
//                    currentStationRef.update("unsafe votes", FieldValue.increment(-1));
//                }
                    //add the user to the current list of SAFE voters and remove it from unsafe voters
                    //Map to add user to array
                    final Map<String, Object> addUserToArrayMap = new HashMap<>();
                    addUserToArrayMap.put("safe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

                    //Map to remove user from array
                    final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
                    removeUserFromArrayMap.put("unsafe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));

                    currentStationRef.update(addUserToArrayMap);
                    currentStationRef.update(removeUserFromArrayMap);

                    //query the number of voters and update it
                    final DocumentReference finalCurrentStationRef = currentStationRef;
                    currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                                List<String> safeVotersList = (List<String>) documentSnapshot.get("safe voters");
                                List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
                                int safeVoters = safeVotersList.size();
                                int unsafeVotes = unsafeVotersList.size();
//                          Toast.makeText(MapActivity.this, "number of safe voters" + (safeVotersList.size() - 1), Toast.LENGTH_SHORT).show();
                                finalCurrentStationRef.update("safe votes", safeVoters);
                                finalCurrentStationRef.update("unsafe votes", unsafeVotes);
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
                }

                /**
                 * todo: need to add this FEED part
                 */
                String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Map<String, Object> vote = new HashMap<>();
                vote.put("safe", true);
                vote.put("station", selectedStation);
                vote.put("timestamp", Timestamp.now());
                vote.put("userId", currentuser);
                if (selectedStation != null) {
                    db.collection("/cities/" + currentCity + "/" + currentCity + " feed")
                            .document(currentuser + selectedStation)
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


                //edit the ui
//                mBtnSafe.setClickable(false);
//                mBtnUnsafe.setClickable(true);
            }
        });

        mBtnUnsafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load an interstitial
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                unsafeBtnClicked = true;
                if (selectedStation == null) {
                    Toast.makeText(MapActivity.this, "Selectionnez une station svp!", Toast.LENGTH_SHORT).show();
                }
                if (selectedStation != null) {
                    DocumentReference currentStationRef =
                            db.collection("cities").document(currentCity).collection(currentCity + " stations").document(selectedStation);
                    //old way of updating the number of votes
//                currentStationRef.update("unsafe votes", FieldValue.increment(1));
//
//                //in case of a correction
//                if (safeBtnClicked == true) {
//                    currentStationRef =
//                            db.collection("cities").document("lille").collection("lille stations").document(selectedStation);
//                    currentStationRef.update("safe votes", FieldValue.increment(-1));
//                }

                    //add the user to the current list of SAFE voters and remove it from unsafe voters
                    //Map to add user to array
                    final Map<String, Object> addUserToArrayMap = new HashMap<>();
                    addUserToArrayMap.put("unsafe voters", FieldValue.arrayUnion(mAuth.getCurrentUser().getUid()));

                    //Map to remove user from array
                    final Map<String, Object> removeUserFromArrayMap = new HashMap<>();
                    removeUserFromArrayMap.put("safe voters", FieldValue.arrayRemove(mAuth.getCurrentUser().getUid()));

                    currentStationRef.update(addUserToArrayMap);
                    currentStationRef.update(removeUserFromArrayMap);

                    //query the number of voters and update it
                    final DocumentReference finalCurrentStationRef = currentStationRef;
                    currentStationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                                List<String> safeVotersList = (List<String>) documentSnapshot.get("safe voters");
                                List<String> unsafeVotersList = (List<String>) documentSnapshot.get("unsafe voters");
                                int safeVoters = safeVotersList.size();
                                int unsafeVotes = unsafeVotersList.size();
//                          Toast.makeText(MapActivity.this, "number of safe voters" + (safeVotersList.size() - 1), Toast.LENGTH_SHORT).show();
                                finalCurrentStationRef.update("safe votes", safeVoters);
                                finalCurrentStationRef.update("unsafe votes", unsafeVotes);
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });

//                mBtnUnsafe.setClickable(false);
//                mBtnSafe.setClickable(true);
                }

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
                            .document(currentuser + selectedStation)
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


    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called!");
        removeMarkersFromMap();
        getDeviceLocation();
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
        Log.d(TAG, "onStart: ");
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "onStart: " + currentUser);
//        currentuser = currentUser;
//        getDeviceLocation();

        if (currentUser == null) {
//            Toast.makeText(MapActivity.this, "sending to start because the current user i null, onStart()!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onStart: currentUser == null");
            sendToStart();
        }

        //ui
        mToolbar = findViewById(R.id.map_toolbar);
        if (selectedStation != null) {
            Log.d(TAG, "onStart: selecteedStation != null");
            mToolbar.setTitle(currentCity + ": " + selectedStation);
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
        Log.d(TAG, "onMarkerClick: called!");
//        String markerTitle = marker.getTitle();
//        Toast.makeText(this, "" + markerTitle, Toast.LENGTH_SHORT).show();
        //show an add
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
        selectedStation = marker.getTitle();
        if (selectedStation != null) {
            Log.d(TAG, "onMarkerClick: selectedStation != null");
            mToolbar.setTitle(currentCity + ": " + selectedStation);
            setSupportActionBar(mToolbar);
        }

        db.collection("cities").document(currentCity).collection(currentCity + " stations").whereEqualTo("location", marker.getPosition())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            selectedStation = document.getId();
                            DocumentReference currentStationRef = db.collection("cities").document(currentCity).collection(currentCity + " stations").document(selectedStation);
                            currentStationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    long safeVotes = 0;
                                    long unsafeVotes = 0;
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                                            Toast.makeText(MapActivity.this, "" + document.getData(), Toast.LENGTH_SHORT).show();
                                            Map<String, Object> stationHashmap = document.getData();
                                            safeVotes = (long) stationHashmap.get("safe votes");
                                            unsafeVotes = (long) stationHashmap.get("unsafe votes");
                                            Toast.makeText(MapActivity.this, String.format(getString(R.string.unsafe), getString(R.string.safe), safeVotes, unsafeVotes), Toast.LENGTH_SHORT).show();
                                            marker.setTag(stationHashmap);
                                            if (selectedStation != null) {
                                                mToolbar.setTitle(currentCity + ": " + selectedStation);
                                                setSupportActionBar(mToolbar);
                                            }


                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());
                                    }
                                }
                            });
                        }

//                        Toast.makeText(MapActivity.this, "SELECTED: " + selectedStation, Toast.LENGTH_SHORT).show();
//                        selectedStationTextView.setText(selectedStation);
                    }
                });
        return false;
    }


    /**
     * enable or disable the safe and notSafe btns
     */
    public void enableUi() {
        Log.d(TAG, "enableUi: called!");
        mBtnSafe.setClickable(true);
        mBtnUnsafe.setClickable(true);
    }

    public void disableUi() {
        Log.d(TAG, "disableUi: called!");
        mBtnSafe.setClickable(false);
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


        if (checkPermissions()) {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
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
//
//        }

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
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                requestPermissionsWithDexter();
                Log.d(TAG, "updateLocationUI: checkPermissions = false");
                mMap.setMyLocationEnabled(false);
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
            FirebaseAuth.getInstance().signOut();
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            sendToStart();
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
                if (mMap != null) {

                    lastLocation = locationResult.getLastLocation();
                    mAuth = FirebaseAuth.getInstance();
                    currentUser = mAuth.getCurrentUser();
//                    Toast.makeText(MapActivity.this, "current use is: " + currentuser, Toast.LENGTH_SHORT).show();
                    if (currentUser == null) {
                        Log.d(TAG, "onLocationResult: sending to start because currentUser object == null");
//                        Toast.makeText(MapActivity.this, "sending to star because you're not logged in, buildlocationCallback", Toast.LENGTH_SHORT).show();
//                        sendToStart();
                    } else {
                        currentuser = mAuth.getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/" + currentuser + "/geofire");
                        geoFire = new GeoFire(ref);
                        geoFire.setLocation(currentuser, new GeoLocation(lastLocation.getLatitude(),
                                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    Toast.makeText(MapActivity.this, "error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
//                                Toast.makeText(MapActivity.this, "location set!", Toast.LENGTH_SHORT).show();
                                    //animate camera

                                }
                            }
                        });
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
                "ca-app-pub-3940256099942544/5224354917");
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
