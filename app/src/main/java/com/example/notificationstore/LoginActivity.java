package com.example.notificationstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
//    EditText editTextEmailAddress, editTextPassword;
//    Button buttonSignin;
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
        setContentView(R.layout.activity_login);

//        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
//        editTextPassword = findViewById(R.id.editTextPassword);
//        buttonSignin = findViewById(R.id.buttonSignin);
//        signupText = findViewById(R.id.signupText);
//        mAuth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//
//        buttonSignin.setOnClickListener(v -> {
//            String email = editTextEmailAddress.getText().toString().trim();
//            String password = editTextPassword.getText().toString().trim();
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
//            } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
//                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
//            } else {
//                loginUser(email, password);
//            }
//        });
//
//        signupText.setOnClickListener(v -> {
//            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
//            startActivity(intent);
//        });
//
//        editTextPassword.setOnTouchListener((view, motionEvent) -> {
//            int DRAWABLE_END = 2;
//            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                if (motionEvent.getRawX() >= (editTextPassword.getRight() - editTextPassword.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
//                    if (isPasswordVisible) {
//                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eye_show_svgrepo_com, 0);
//                    } else {
//                        editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eyeoff, 0);
//                    }
//                    isPasswordVisible = !isPasswordVisible;
//                    editTextPassword.setSelection(editTextPassword.getText().length());
//
//                    view.performClick();
//                    return true;
//                }
//            }
//            return false;
//        });
//    }
//
//    private void loginUser(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                FirebaseUser user = mAuth.getCurrentUser();
//                if (user != null) {
//                    Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }
}