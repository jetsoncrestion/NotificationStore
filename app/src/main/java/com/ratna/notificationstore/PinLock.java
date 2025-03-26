package com.ratna.notificationstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PinLock extends AppCompatActivity {
    private static final String TAG = "PinLock";
    private TextView pin1, pin2, pin3, pin4, textViewForgetPin;
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button0, buttonOk;
    private ImageView imageViewDelete;
    private StringBuilder enteredPin = new StringBuilder();
    private int attempts = 0;
    private static int MAX_ATTEMPTS = 3;
    //private String savedPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pin_lock);
        pin1 = findViewById(R.id.pin1);
        pin2 = findViewById(R.id.pin2);
        pin3 = findViewById(R.id.pin3);
        pin4 = findViewById(R.id.pin4);
        textViewForgetPin = findViewById(R.id.textViewForgetYourPin);
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

      //  savedPin = getStoredPin(this);
        setDigitButtonClickListeners();
        imageViewDelete.setOnClickListener(v -> deleteDigit());
        buttonOk.setOnClickListener(v -> validatePin());

        textViewForgetPin.setOnClickListener(view -> {
            Intent intent = new Intent(PinLock.this, ForgetPin.class);
            startActivity(intent);
            finish();
        });
    }

    private void setDigitButtonClickListeners() {
        Button[] buttons = {button1, button2, button3, button4, button5, button6, button7, button8, button9, button0};
        for (Button button : buttons) {
            button.setOnClickListener(v -> {
                if (enteredPin.length() < 4) {
                    enteredPin.append(button.getText().toString());
                    updatePinDisplay();
                }
            });
        }
    }

    private void deleteDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        String pin = enteredPin.toString();
        int length = pin.length();
        pin1.setText(length >= 1 ? "*" : "");
        pin2.setText(length >= 2 ? "*" : "");
        pin3.setText(length >= 3 ? "*" : "");
        pin4.setText(length >= 4 ? "*" : "");
    }
    private void validatePin() {
        String storedPin = getStoredPin(this);
        if (storedPin == null) {
            startActivity(new Intent(PinLock.this, MainActivity.class));
            finish();
            return;
        }

        if (enteredPin.toString().equals(storedPin)) {
            // Correct PIN → Proceed to MainActivity
            Toast.makeText(this, "PIN correct!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PinLock.this, MainActivity.class));
            finish();
        } else {
            attempts++;
            int remainingAttempts = MAX_ATTEMPTS - attempts;
            if (remainingAttempts > 0) {
                Toast.makeText(this, "Incorrect PIN! Attempts left: " + remainingAttempts, Toast.LENGTH_SHORT).show();
            }
            enteredPin.setLength(0);
            updatePinDisplay();

            if (attempts >= MAX_ATTEMPTS) {
                Toast.makeText(this, "Too many failed attempts! Please reset your PIN.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PinLock.this, ForgetPin.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public static String getStoredPin(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    "secure_pin_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return sharedPreferences.getString("PIN", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}