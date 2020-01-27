package com.alainsaris.stitcha;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MarkerChooserPopup extends AppCompatActivity implements View.OnClickListener {
    EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_chooser_popup);


        /**
         * make it look like a fuckign popup
         */
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.7), (int) (height * 0.7));


        ImageView policeMarker = findViewById(R.id.imageViewPoliceMarker);
        ImageView ticketsMarker = findViewById(R.id.imageViewTicketsMarker);
        ImageView fireMarker = findViewById(R.id.imageViewFireMarker);
        ImageView protestsMarker = findViewById(R.id.imageViewProtestsMarker);

        messageEditText = findViewById(R.id.popup_message_edit_text);

        policeMarker.setOnClickListener(this);
        ticketsMarker.setOnClickListener(this);
        fireMarker.setOnClickListener(this);
        protestsMarker.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        Intent returnIntent = new Intent();

        switch (view.getId()) {
            case R.id.imageViewPoliceMarker:
//                Toast.makeText(this, "selected Police!", Toast.LENGTH_SHORT).show();
                returnIntent.putExtra("type", "policeMarker");

                break;
            case R.id.imageViewTicketsMarker:
                returnIntent.putExtra("type", "ticketsMarker");
                break;
            case R.id.imageViewFireMarker:
                returnIntent.putExtra("type", "fireMarker");
                break;
            case R.id.imageViewProtestsMarker:
                returnIntent.putExtra("type", "protestMarker");
                break;

        }
        returnIntent.putExtra("message", messageEditText.getText().toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }
}
