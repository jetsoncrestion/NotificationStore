package com.example.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeFourth extends AppCompatActivity {
    private static final String TAG = "WelcomeFourth";
    private static final String SHARED_PREFS = "shared_prefs";
    private static final String KEY_POLICIES_ACCEPTED = "policies_accepted";

    private Button buttonStart;
    private CheckBox checkBox;
    private TextView textViewCheckboxTitle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Check if policies are already accepted
        boolean policiesAccepted = sharedPreferences.getBoolean(KEY_POLICIES_ACCEPTED, false);

        Log.d(TAG, "Policies accepted: " + policiesAccepted);

        if (policiesAccepted) {
            // Navigate directly if policies are already accepted
            navigateToPermissionEnable();
            return;
        }

        // Set the layout for policy acceptance screen
        setContentView(R.layout.activity_welcome_fourth);

        buttonStart = findViewById(R.id.buttonStart);
        checkBox = findViewById(R.id.checkBox);
        textViewCheckboxTitle = findViewById(R.id.textViewCheckboxTitle);

        // Text for policy acceptance
        String text = "By clicking on \"Start\" I confirm that I have read and accept the Terms & Conditions and the Privacy Policy.";
        SpannableString spannableString = new SpannableString(text);

        int termsStart = text.indexOf("Terms & Conditions");
        int termsEnd = termsStart + "Terms & Conditions".length();
        spannableString.setSpan(new TouchableSpan(v -> {
            Intent intent = new Intent(WelcomeFourth.this, TermsAndConditionsActivity.class);
            startActivity(intent);
        }), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyStart = text.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();
        spannableString.setSpan(new TouchableSpan(v -> {
            Intent intent = new Intent(WelcomeFourth.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        }), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewCheckboxTitle.setText(spannableString);
        textViewCheckboxTitle.setMovementMethod(LinkMovementMethod.getInstance());

        buttonStart.setEnabled(false);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonStart.setEnabled(isChecked);
            buttonStart.setBackgroundResource(isChecked ? R.drawable.button_enable : R.drawable.button_disabled);
        });

        buttonStart.setOnClickListener(v -> {
            // Save acceptance status in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_POLICIES_ACCEPTED, true); // Mark as completed
            editor.apply();

            navigateToPermissionEnable();
        });
    }

    private void navigateToPermissionEnable() {
        Log.d(TAG, "Navigating to Permission Enable Screen");
        Intent intent = new Intent(WelcomeFourth.this, AppSelectionSecond.class);
        startActivity(intent);
        finish();
    }

    private static class TouchableSpan extends ClickableSpan {
        private final View.OnClickListener onClickListener;

        public TouchableSpan(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @Override
        public void onClick(View widget) {
            if (onClickListener != null) {
                onClickListener.onClick(widget);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.BLUE); // Custom link color
            ds.setUnderlineText(false); // Remove underline
        }
    }
}
