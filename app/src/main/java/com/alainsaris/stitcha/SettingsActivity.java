package com.alainsaris.stitcha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    //ui
    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //ui
        mDisplayImage = findViewById(R.id.profile_image_settings);
        mStatus = findViewById(R.id.settings_status);
        mDisplayName = findViewById(R.id.settings_display_name);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String name = dataSnapshot.child("name").getValue().toString();
//                String image = dataSnapshot.child("image").getValue().toString();
//                String status = dataSnapshot.child("status").getValue().toString();
//                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

//                mDisplayName.setText(name);
//                mStatus.setText(status);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
