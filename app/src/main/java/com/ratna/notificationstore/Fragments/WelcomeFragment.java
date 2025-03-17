package com.ratna.notificationstore.Fragments;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ratna.notificationstore.R;
import com.ratna.notificationstore.Welcome;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        //TextView textViewSkip = view.findViewById(R.id.textViewSkip);
//        textViewSkip.setOnClickListener(v -> {
//            // Mark welcome screen as shown and navigate to the main activity
//            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", 0);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("welcome_screen_shown", true);
//            editor.apply();
//
//            ((Welcome) requireActivity()).navigateToMain();
//        });
        return view;
    }
}