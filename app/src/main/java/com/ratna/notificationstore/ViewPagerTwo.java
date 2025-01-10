package com.ratna.notificationstore;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerTwo extends AppCompatActivity {
  // private ViewPager2 viewPagerTwo;
    private TabLayout tabLayout;
    private ImageView imageViewLeft, imageViewRight;
    private TextView skipText;
    private List<Integer> layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_pager2);

       // viewPagerTwo = findViewById(R.id.viewPagerTwo);
        tabLayout = findViewById(R.id.tabLayout);
        imageViewLeft = findViewById(R.id.imageViewLeft);
        imageViewRight = findViewById(R.id.imageViewRight);
        skipText = findViewById(R.id.textViewSkip);

        List<Integer> layouts = new ArrayList<>();
        layouts.add(R.layout.activity_welcome);
        layouts.add(R.layout.activity_welcome_second);
        layouts.add(R.layout.activity_welcome_third);
        layouts.add(R.layout.activity_welcome_fourth);

        ViewPagerAdapter adapter = new ViewPagerAdapter(layouts);
        ViewPager2 viewPagerTwo = findViewById(R.id.viewPagerTwo);
        viewPagerTwo.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPagerTwo, (tab, position) -> {
            tab.setText("Page " + (position + 1));
        }).attach();

        skipText.setOnClickListener(v -> {
            viewPagerTwo.setCurrentItem(layouts.size() - 1, true);
        });

        imageViewLeft.setOnClickListener(v -> {
            int currentItem = viewPagerTwo.getCurrentItem();
            if (currentItem > 0) {
                viewPagerTwo.setCurrentItem(currentItem - 1, true);
            }
        });

        imageViewRight.setOnClickListener(v -> {
            int currentItem = viewPagerTwo.getCurrentItem();
            if (currentItem < layouts.size() - 1) {
                viewPagerTwo.setCurrentItem(currentItem + 1, true);
            }
        });

        viewPagerTwo.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                imageViewLeft.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
                imageViewRight.setVisibility(position == layouts.size() - 1 ? View.INVISIBLE : View.VISIBLE);
                skipText.setVisibility(position == layouts.size() - 1 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }
}