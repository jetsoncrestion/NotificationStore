package com.example.notificationstore;

import android.content.SharedPreferences;
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

public class ChangePin extends AppCompatActivity {
    private TextInputEditText oldPin_textInput, newPin_textInput, confirmNewPin_textInput;
    private Button buttonSaveChanges;
    private DatabaseReference databaseRef;
    private String deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pin);

        oldPin_textInput = findViewById(R.id.oldPin_textInput);
        newPin_textInput = findViewById(R.id.newPin_textInput);
        confirmNewPin_textInput = findViewById(R.id.confirmNewPin_textInput);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        databaseRef = FirebaseDatabase.getInstance().getReference("devices");

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        deviceId = sharedPreferences.getString("deviceId", null);

        if (TextUtils.isEmpty(deviceId)) {
            Toast.makeText(this, "Device ID is not initialized.", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonSaveChanges.setOnClickListener(v -> changePin());
    }

    private void changePin() {
        String oldPin = oldPin_textInput.getText().toString().trim();
        String newPin = newPin_textInput.getText().toString().trim();
        String confirmNewPin = confirmNewPin_textInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(oldPin) || oldPin.length() != 4) {
            Toast.makeText(this, "Enter a valid 4-digit old PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPin) || newPin.length() != 4) {
            Toast.makeText(this, "Enter a valid 4-digit new PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.equals(confirmNewPin)) {
            Toast.makeText(this, "New PIN and Confirm PIN do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify old PIN
        DatabaseReference pinRef = databaseRef.child(deviceId).child("pin");
        pinRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedPin = snapshot.child("pin").getValue(String.class);
                    if (storedPin != null && storedPin.equals(oldPin)) {
                        // Old PIN matches, update with the new PIN
                        updatePin(pinRef, newPin);
                    } else {
                        Toast.makeText(ChangePin.this, "Old PIN is incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePin.this, "No PIN found. Set up a new PIN first.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChangePin.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePin(DatabaseReference pinRef, String newPin) {
        pinRef.child("pin").setValue(newPin)
                .addOnSuccessListener(aVoid -> Toast.makeText(ChangePin.this, "PIN changed successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ChangePin.this, "Failed to change PIN: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
