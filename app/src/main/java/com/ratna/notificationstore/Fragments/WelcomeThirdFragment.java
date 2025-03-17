package com.ratna.notificationstore.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ratna.notificationstore.AppSelectionSecond;
import com.ratna.notificationstore.R;

public class WelcomeThirdFragment extends Fragment {
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_WELCOME_SCREEN_SHOWN = "welcome_screen_shown";

    public WelcomeThirdFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_third, container, false);

        Button buttonGetStarted = view.findViewById(R.id.buttonGetStarted);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);

        buttonGetStarted.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_WELCOME_SCREEN_SHOWN, true);
            editor.apply();
            navigateToPermissionEnable();
        });
        return view;
    }

    private void navigateToPermissionEnable() {
        Intent intent = new Intent(getActivity(), AppSelectionSecond.class);
        startActivity(intent);
        getActivity().finish();
    }
}
