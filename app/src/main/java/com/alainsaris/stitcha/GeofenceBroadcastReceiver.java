package com.alainsaris.stitcha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getName();

    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
        Toast.makeText(context, "broadcastReceiverOnReceive called!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onReceive: broadcast reaciver on receive called");
        MapActivity mapActivity = new MapActivity();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            Toast.makeText(context, "entered the geofence!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onReceive: ENTERED THE FUCKING GEOFENCE BROOOOOOOOO!!!!!!");


            mapActivity.enableUi();
            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//            );

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);
//            Log.i(TAG, geofenceTransitionDetails);
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Toast.makeText(context, "exited the geofence!", Toast.LENGTH_SHORT).show();
                mapActivity.disableUi();
                Log.d(TAG, "onReceive: EXITED THE FUCKING GEOFENCE BROOOOOOOOO!!!!!!");
            }
        } else {
            // Log the error.
            Log.e(TAG, Resources.getSystem().getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
            Toast.makeText(mapActivity, "Geofence transition error: invalid transition type!!", Toast.LENGTH_SHORT).show();
        }


    }
}
