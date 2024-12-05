package com.example.notificationstore;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
//    EditText editTextName, editTextEmailAddress, editTextPassword, editTextConfirmPassword;
//    Button buttonSignUp;
//    TextView signupText;
//    private String Name, Email, Password, ConfirmPassword;
//    private FirebaseAuth mAuth;
//    private DatabaseReference databaseReference;
//    boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

//        editTextName = findViewById(R.id.editTextName);
//        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
//        editTextPassword = findViewById(R.id.editTextPassword);
//        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
//        buttonSignUp = findViewById(R.id.buttonSignup);
//        signupText = findViewById(R.id.signupText);
//
//        mAuth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//
//        setupPasswordVisibilityToggle(editTextPassword);
//        setupPasswordVisibilityToggle(editTextConfirmPassword);
//
//        buttonSignUp.setOnClickListener(v -> {
//            Name = editTextName.getText().toString().trim();
//            Email = editTextEmailAddress.getText().toString().trim();
//            Password = editTextPassword.getText().toString().trim();
//            ConfirmPassword = editTextConfirmPassword.getText().toString().trim();
//
//            if (validateInputs()) {
//                createAccount(Email, Password);
//            }
//        });
//
//        signupText.setOnClickListener(v -> {
//            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//            startActivity(intent);
//        });
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private void setupPasswordVisibilityToggle(EditText editText) {
//        editText.setOnTouchListener((view, motionEvent) -> {
//            int DRAWABLE_END = 2;
//            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                if (motionEvent.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
//                    togglePasswordVisibility(editText);
//                    return true;
//                }
//            }
//            return false;
//        });
//    }
//
//    private void togglePasswordVisibility(EditText editText) {
//        if (isPasswordVisible) {
//            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eye_show_svgrepo_com, 0);
//        } else {
//            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eyeoff, 0);
//        }
//        isPasswordVisible = !isPasswordVisible;
//        editText.setSelection(editText.getText().length());
//    }
//
//    private void createAccount(String email, String password) {
//        checkIfEmailExists(email, exists -> {
//            if (exists) {
//                Toast.makeText(this, "Email already in used", Toast.LENGTH_SHORT).show();
//            } else {
//                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        saveUserData();
//                        Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//    }
//
//    private void checkIfEmailExists(String email, OnCheckCompleteListener listener) {
//        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    listener.onCheckComplete(true);
//                } else {
//                    listener.onCheckComplete(false); // Notify that email does not exist
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "onCancelled: DatabaseError", error.toException());
//            }
//        });
//    }
//
//    private boolean validateInputs() {
//        if (Name.isEmpty() && Email.isEmpty() && Password.isEmpty() && ConfirmPassword.isEmpty()) {
//            Toast.makeText(this, "All fields are missing. Please fill in the required information.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (Name.isEmpty()) {
//            Toast.makeText(this, "Name field is missing. Please enter your name.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (Email.isEmpty()) {
//            Toast.makeText(this, "Email field is missing. Please enter your email.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (!isValidEmail(Email)) {
//            Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (Password.isEmpty()) {
//            Toast.makeText(this, "Password field is missing. Please enter your password.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (!isValidPassword(Password)) {
//            Toast.makeText(this, "Password must be more than 8 characters long.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (ConfirmPassword.isEmpty()) {
//            Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (!Password.equals(ConfirmPassword)) {
//            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }
//
//    private boolean isValidEmail(String email) {
//        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
//    }
//
//    private boolean isValidPassword(String password) {
//        return password.length() > 8;
//    }
//
//    private void saveUserData() {
//        Name = editTextName.getText().toString().trim();
//        Email = editTextEmailAddress.getText().toString().trim();
//        Password = editTextPassword.getText().toString().trim();
//        ConfirmPassword = editTextConfirmPassword.getText().toString().trim();
//
//        User user = new User(Name, Email, Password, ConfirmPassword);
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        databaseReference.child(userId).setValue(user);
//    }
//
//    interface OnCheckCompleteListener {
//        void onCheckComplete(boolean exists);
   }
}