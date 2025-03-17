package com.ratna.notificationstore.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ratna.notificationstore.Fragments.WelcomeFragment;
import com.ratna.notificationstore.Fragments.WelcomeSecondFragment;
import com.ratna.notificationstore.Fragments.WelcomeThirdFragment;

public class WelcomePagerAdapter extends FragmentStateAdapter {
    public WelcomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WelcomeFragment(); // First fragment is WelcomeFragment
            case 1:
                return new WelcomeSecondFragment(); // Second fragment is WelcomeSecondFragment
            case 2:
                return new WelcomeThirdFragment(); // Third fragment is WelcomeThirdFragment
            default:
                return new WelcomeFragment(); // Default case (should not happen)
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
