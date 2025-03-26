package com.ratna.notificationstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgetPin extends AppCompatActivity {
    private EditText editTextSecurityAnswer, editTextSecurityAnswer2, editTextSecurityAnswer3;
    private Button buttonSubmit;
    private TextView textViewBackToLogin, textViewSecurityQuestion, textViewSecurityQuestion2, textViewSecurityQuestion3;
    private int securityQuestionAttempts = 0;
    private static final int MAX_SECURITY_ATTEMPTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_pin);

        editTextSecurityAnswer = findViewById(R.id.editTextSecurityAnswer);
        editTextSecurityAnswer2 = findViewById(R.id.editTextSecurityAnswer2);
        editTextSecurityAnswer3 = findViewById(R.id.editTextSecurityAnswer3);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);
        textViewSecurityQuestion = findViewById(R.id.editTextSecurityQuestion);
        textViewSecurityQuestion2 = findViewById(R.id.editTextSecurityQuestion2);
        textViewSecurityQuestion3 = findViewById(R.id.editTextSecurityQuestion3);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String savedQuestion1 = sharedPreferences.getString("SECURITY_QUESTION_1", "Question 1 not set");
        String savedQuestion2 = sharedPreferences.getString("SECURITY_QUESTION_2", "Question 2 not set");
        String savedQuestion3 = sharedPreferences.getString("SECURITY_QUESTION_3", "Question 3 not set");
        final String savedAnswer1 = sharedPreferences.getString("SECURITY_ANSWER_1", "");
        final String savedAnswer2 = sharedPreferences.getString("SECURITY_ANSWER_3", "");
        final String savedAnswer3 = sharedPreferences.getString("SECURITY_ANSWER_3", "");


        textViewSecurityQuestion.setText(savedQuestion1);
        textViewSecurityQuestion2.setText(savedQuestion2);
        textViewSecurityQuestion3.setText(savedQuestion3);

        buttonSubmit.setOnClickListener((View.OnClickListener) v -> {
            securityQuestionAttempts++;
            if (securityQuestionAttempts > MAX_SECURITY_ATTEMPTS) {
                Toast.makeText(this, "Too many failed attempts. Please try again later.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            String answer1 = editTextSecurityAnswer.getText().toString().trim();
            String answer2 = editTextSecurityAnswer2.getText().toString().trim();
            String answer3 = editTextSecurityAnswer3.getText().toString().trim();

            int correctCount = 0;
            if (!answer1.isEmpty() && answer1.equalsIgnoreCase(savedAnswer1)) correctCount++;
            if (!answer2.isEmpty() && answer2.equalsIgnoreCase(savedAnswer2)) correctCount++;
            if (!answer3.isEmpty() && answer3.equalsIgnoreCase(savedAnswer3)) correctCount++;

            if (correctCount >= 2) {
                Toast.makeText(ForgetPin.this, "Correct answers! Now reset your PIN", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ForgetPin.this, ResetPinActivity.class));
                finish();
            } else {
                int remainingAttempts = MAX_SECURITY_ATTEMPTS - securityQuestionAttempts;
                if (remainingAttempts > 0) {
                    Toast.makeText(this,
                            String.format("%d/5 correct. Need at least 2. Attempts left: %d",
                                    correctCount, remainingAttempts),
                            Toast.LENGTH_LONG).show();
                }
                // Clear fields for next attempt
                editTextSecurityAnswer.setText("");
                editTextSecurityAnswer2.setText("");
                editTextSecurityAnswer3.setText("");
            }
        });

        // Back to Login functionality
        textViewBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgetPin.this, PinLock.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
