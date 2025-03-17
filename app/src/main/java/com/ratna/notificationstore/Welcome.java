package com.ratna.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ratna.notificationstore.Adapter.WelcomePagerAdapter;

public class Welcome extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_WELCOME_SCREEN_SHOWN = "welcome_screen_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isWelcomeScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_SCREEN_SHOWN, false);

        if (isWelcomeScreenShown) {
            navigateToMain();
            return;
        }

        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        TextView textViewSkip = findViewById(R.id.textViewSkip);
        ImageView imageViewArrowRight = findViewById(R.id.imageViewArrowRight);


        WelcomePagerAdapter adapter = new WelcomePagerAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {}).attach();

        textViewSkip.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_WELCOME_SCREEN_SHOWN, true);
            editor.apply();
            navigateToMain();
        });

        imageViewArrowRight.setOnClickListener(v -> {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager2.setCurrentItem(currentItem + 1, true);
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Show Skip and Right Arrow only in the first and second fragments
                if (position == 0 || position == 1) {
                    textViewSkip.setVisibility(View.VISIBLE);
                    imageViewArrowRight.setVisibility(View.VISIBLE);
                } else {
                    // Hide Skip and Right Arrow in the third fragment
                    textViewSkip.setVisibility(View.GONE);
                    imageViewArrowRight.setVisibility(View.GONE);
                }
            }
        });
    }

    public void navigateToMain() {
        Intent intent = new Intent(Welcome.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    }