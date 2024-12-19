package com.example.notificationstore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class SecurityActivity extends AppCompatActivity {
    private Button btn_ok, buttonForgetPin, buttonChangePin, btn_delete;
    private EditText etPin;
    private DatabaseReference databaseRef;
    private String deviceId;
    private StringBuilder enteredPin = new StringBuilder();

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);

        etPin = findViewById(R.id.et_pin);
        btn_ok = findViewById(R.id.btn_ok);
        buttonForgetPin = findViewById(R.id.buttonForgetPin);
        buttonChangePin = findViewById(R.id.buttonChangePin);
        btn_delete = findViewById(R.id.btn_delete);

        databaseRef = FirebaseDatabase.getInstance().getReference("devices");

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        deviceId = sharedPreferences.getString("deviceId", null);

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            sharedPreferences.edit().putString("deviceId", deviceId).apply();
        }

        checkExistingPin();

        buttonChangePin.setOnClickListener(v -> {
            Intent intent = new Intent(SecurityActivity.this, ChangePin.class);
            startActivity(intent);
        });

        buttonForgetPin.setOnClickListener(v -> {
            Intent intent = new Intent(SecurityActivity.this, ForgetPin.class);
            startActivity(intent);
        });

        btn_delete.setOnClickListener(v -> deleteLastDigit());

        btn_ok.setOnClickListener(v -> validateOrSavePin());

        // Setup keypad button clicks
        setupKeypad();
    }

    private void setupKeypad() {
        int[] buttonIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int id : buttonIds) {
            Button numberButton = findViewById(id);
            numberButton.setOnClickListener(v -> appendToPin(numberButton.getText().toString()));
        }
    }

    private void appendToPin(String digit) {
        if (enteredPin.length() < 4) { // Limit PIN to 4 digits
            enteredPin.append(digit);
            etPin.setText(enteredPin.toString());
        }
    }

    private void deleteLastDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            etPin.setText(enteredPin.toString());
        }
    }

    private void checkExistingPin() {
        DatabaseReference pinRef = databaseRef.child(deviceId).child("pin");

        pinRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // PIN exists: disable new PIN setup
                    btn_ok.setEnabled(false);
                    Toast.makeText(SecurityActivity.this, "A PIN is already set. Use Forget or Change PIN options.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SecurityActivity.this, "Error checking PIN: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateOrSavePin() {
        String pin = enteredPin.toString();

        // Validate PIN length
        if (pin.length() != 4) {
            Toast.makeText(this, "Enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure deviceId is initialized
//        if (TextUtils.isEmpty(deviceId)) {
//            Toast.makeText(this, "Device ID is not initialized.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        DatabaseReference pinRef = databaseRef.child(deviceId).child("pin");

        // Reference to the pin node directly under the deviceId
       // DatabaseReference pinRef = databaseRef.child(deviceId).child("pin");

        pinRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // PIN exists: validate
                    String storedPin = snapshot.getValue(String.class);
                    if (pin.equals(storedPin)) {
                        Toast.makeText(SecurityActivity.this, "PIN validated successfully!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(SecurityActivity.this, "Invalid PIN! Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No PIN exists: save new PIN
                    savePinToFirebase(pin);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SecurityActivity.this, "Error validating PIN: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void savePinToFirebase(String pin) {

        // Store the PIN directly under the deviceId, no need for a "pin" node.
        DatabaseReference pinRef = databaseRef.child(deviceId);
        String recoveryKey = generateRecoveryKey();
        pinRef.child("pin").setValue(pin);
        pinRef.child("recoveryKey").setValue(recoveryKey)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "PIN saved successfully!", Toast.LENGTH_SHORT).show();
                    showRecoveryKeyDialog(recoveryKey);
                    markPinSetupComplete();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save PIN: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateRecoveryKey() {
        Random random = new Random();
        int recoveryKey = 100000 + random.nextInt(900000); // Generate a 6-digit number
        return String.valueOf(recoveryKey);
    }

    private void showRecoveryKeyDialog(String recoveryKey) {
        new AlertDialog.Builder(this)
                .setTitle("Recovery Key")
                .setMessage("Your recovery key is: " + recoveryKey +
                        "\n\nPlease save this key securely. You will need it if you forget your PIN.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

//    private void deletePin() {
//        // Remove the pin directly under the deviceId node
//        databaseRef.child(deviceId).child("pin").removeValue()
//                .addOnSuccessListener(aVoid -> Toast.makeText(this, "PIN deleted successfully!", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete PIN: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }

    private void markPinSetupComplete() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isPinSet", true);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SecurityActivity.this, SplashScreen.class);
        startActivity(intent);
        finish();
    }
}
