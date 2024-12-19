package com.example.notificationstore;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgetPin extends AppCompatActivity {
    private TextInputEditText recoveryPin_textInput, newPin_textInput, conformNewPin_textInput;
    private Button buttonSaveChanges;
    private DatabaseReference databaseRef;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_pin);

        recoveryPin_textInput = findViewById(R.id.recoveryPin_textInput);
        newPin_textInput = findViewById(R.id.newPin_textInput);
        conformNewPin_textInput = findViewById(R.id.conformNewPin_textInput);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        databaseRef = FirebaseDatabase.getInstance().getReference("devices");
        deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        buttonSaveChanges.setOnClickListener(v -> verifyRecoveryKeyAndResetPin());
    }

    private void verifyRecoveryKeyAndResetPin() {
        String enteredRecoveryKey = recoveryPin_textInput.getText().toString();
        String newPin = newPin_textInput.getText().toString();
        String confirmNewPin = conformNewPin_textInput.getText().toString();

        if (TextUtils.isEmpty(enteredRecoveryKey) || TextUtils.isEmpty(newPin) || TextUtils.isEmpty(confirmNewPin)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.equals(confirmNewPin)) {
            Toast.makeText(this, "New PIN and confirmation do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPin.length() != 4) {
            Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedRecoveryKey = snapshot.child("recoveryKey").getValue(String.class);

                    if (storedRecoveryKey != null && storedRecoveryKey.equals(enteredRecoveryKey)) {
                        // Recovery key matches; reset PIN
                        databaseRef.child(deviceId).child("pin").setValue(newPin)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ForgetPin.this, "PIN reset successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(ForgetPin.this, "Failed to reset PIN", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(ForgetPin.this, "Invalid recovery key", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgetPin.this, "Device not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ForgetPin.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}