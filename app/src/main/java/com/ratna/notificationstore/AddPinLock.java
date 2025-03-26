package com.ratna.notificationstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AddPinLock extends AppCompatActivity {
    private LinearLayout PinDisplayBox;
    private TextView pin1, pin2, pin3, pin4;
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button0, buttonOk;
    private ImageView imageViewDelete;
    private StringBuilder pinBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pin_lock);
        PinDisplayBox = findViewById(R.id.PinDisplayBox);
        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button0 = findViewById(R.id.button0);
        buttonOk = findViewById(R.id.buttonOk);
        imageViewDelete = findViewById(R.id.imageViewDelete);

        setDigitButtonClickListeners();
        imageViewDelete.setOnClickListener(v -> deleteDigit());
        buttonOk.setOnClickListener(v -> confirmPin());
    }

    private void setDigitButtonClickListeners() {
        View.OnClickListener digitClickListener = v -> {
            Button button = (Button) v;
            String digit = button.getText().toString();
            appendDigit(digit);
        };
        button1.setOnClickListener(digitClickListener);
        button2.setOnClickListener(digitClickListener);
        button3.setOnClickListener(digitClickListener);
        button4.setOnClickListener(digitClickListener);
        button5.setOnClickListener(digitClickListener);
        button6.setOnClickListener(digitClickListener);
        button7.setOnClickListener(digitClickListener);
        button8.setOnClickListener(digitClickListener);
        button9.setOnClickListener(digitClickListener);
        button0.setOnClickListener(digitClickListener);
    }

    private void appendDigit(String digit) {
        if (pinBuilder.length() < 4) {
            pinBuilder.append(digit);
            updatePinDisplay();
        }
    }

    private void deleteDigit() {
        if (pinBuilder.length() > 0) {
            pinBuilder.deleteCharAt(pinBuilder.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        String pin = pinBuilder.toString();
        int length = pin.length();

        pin1.setText(length >= 1 ? "*" : "");
        pin2.setText(length >= 2 ? "*" : "");
        pin3.setText(length >= 3 ? "*" : "");
        pin4.setText(length >= 4 ? "*" : "");
    }

    private void confirmPin() {
        if (pinBuilder.length() == 4) {
            savePin(pinBuilder.toString());
            startActivity(new Intent(AddPinLock.this, SetSecurityQuestionActivity.class));
            finish();
        } else {
            // Handle the case where the PIN is not 4 digits long
            Toast.makeText(this, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePin(String pin) {
       try {
           // Create or retrieve the m,aster key
           String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
           // Initialize EncryptedSharedPreferences
           EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                   "secure_pin_prefs",
                   masterKeyAlias,
                   this,
                   EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                   EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
           );
           sharedPreferences.edit()
                   .putString("PIN", pin)
                   .apply();
           Toast.makeText(this, "PIN saved securely", Toast.LENGTH_SHORT).show();
       } catch (GeneralSecurityException | IOException e) {
           Toast.makeText(this, "Failed to save PIN securely", Toast.LENGTH_SHORT).show();
           e.printStackTrace();
           Toast.makeText(this, "Failed to save PIN securely", Toast.LENGTH_SHORT).show();
       }
    }

    public static String getPin(Context context) {
        try {
            // Create or retrieve the master key
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            // Initialize EncryptedSharedPreferences
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "secure_pin_prefs", // Name of the shared preferences file
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            // Retrieve the PIN
            return sharedPreferences.getString("PIN", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}