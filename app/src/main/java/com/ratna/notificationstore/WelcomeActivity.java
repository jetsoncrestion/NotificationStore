//package com.ratna.notificationstore;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.viewpager2.widget.ViewPager2;
//
//import com.google.android.material.tabs.TabLayout;
//import com.google.android.material.tabs.TabLayoutMediator;
//import com.ratna.notificationstore.Adapter.WelcomePagerAdapter;
//
//public class WelcomeActivity extends AppCompatActivity {
//    private static final String PREFS_NAME = "MyPrefs";
//    private static final String KEY_WELCOME_SCREEN_SHOWN = "welcome_screen_shown";
//    private TextView textViewSkip;
//    private ImageView imageViewArrowRight;
//    private ViewPager2 viewPager2;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_welcome);
//
//        textViewSkip = findViewById(R.id.textViewSkip);
//        imageViewArrowRight = findViewById(R.id.imageViewArrowRight);
//        viewPager2 = findViewById(R.id.viewPager);
//        TabLayout tabLayout = findViewById(R.id.tabLayout);
//
//        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        boolean isWelcomeScreenShown = sharedPreferences.getBoolean(KEY_WELCOME_SCREEN_SHOWN, false);
//
//        if (isWelcomeScreenShown) {
//            navigateToMain();
//            return;
//        }
//
//        WelcomePagerAdapter adapter = new WelcomePagerAdapter(this);
//        viewPager2.setAdapter(adapter);
//
//        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {}).attach();
//
//        textViewSkip.setOnClickListener(v -> {
//            setWelcomeScreenShown(sharedPreferences);
//            navigateToMain();
//        });
//        imageViewArrowRight.setOnClickListener(v -> {
//            if (viewPager2.getCurrentItem() < 2) {
//                viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
//            } else {
//                setWelcomeScreenShown(sharedPreferences);
//                navigateToMain();
//            }
//        });
//    }
//    private void setWelcomeScreenShown(SharedPreferences sharedPreferences) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(KEY_WELCOME_SCREEN_SHOWN, true);
//        editor.apply();
//    }
//
//    private void navigateToMain() {
//        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
//    }