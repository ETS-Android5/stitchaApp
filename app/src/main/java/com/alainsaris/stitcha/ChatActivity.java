package com.alainsaris.stitcha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ChatActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;

    //firebase
    private FirebaseAuth mAuth;

    //ui
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat mode baby!");

        //tabs
        mViewPager = findViewById(R.id.main_tab_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), 1);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
//        if (item.getItemId() == R.id.main_logout_btn) {
//            FirebaseAuth.getInstance().signOut();
//            sendToStart();
//        }
//        if (item.getItemId() == R.id.main_account_settings_btn) {
//            Intent settingsIntent = new Intent(ChatActivity.this, SettingsActivity.class);
//            startActivity(settingsIntent);
//        }

        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(ChatActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();//not comeback when back button is pressed
    }
}
