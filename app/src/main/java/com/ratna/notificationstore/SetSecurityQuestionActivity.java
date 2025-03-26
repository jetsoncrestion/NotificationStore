package com.ratna.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetSecurityQuestionActivity extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteTextViewQuestion, autoCompleteTextViewQuestion2, autoCompleteTextViewQuestion3;
    private com.google.android.material.textfield.TextInputEditText editTextSecurityAnswer, editTextSecurityAnswer2, editTextSecurityAnswer3;
    private Button buttonSubmit;
    private final String[] securityQuestions = {
            "What is your mother’s maiden name?",
            "What was the name of your first pet?",
            "What was the name of your first school?",
            "What was the make and model of your first car?",
            "What is your favorite childhood book?",
            "What was your first job title?",
            "What is the name of your childhood best friend?",
            "What was the destination of your first flight?",
            "What is your favorite holiday destination?",
            "What is your maternal grandmother’s first name?"
    };
    private List<String> availableQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_security_question);
        autoCompleteTextViewQuestion = findViewById(R.id.autoCompleteTextViewQuestion);
        autoCompleteTextViewQuestion2 = findViewById(R.id.autoCompleteTextViewQuestion2);
        autoCompleteTextViewQuestion3 = findViewById(R.id.autoCompleteTextViewQuestion3);
        editTextSecurityAnswer = findViewById(R.id.editTextSecurityAnswer);
        editTextSecurityAnswer2 = findViewById(R.id.editTextSecurityAnswer2);
        editTextSecurityAnswer3 = findViewById(R.id.editTextSecurityAnswer3);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        availableQuestions = new ArrayList<>(Arrays.asList(securityQuestions));

        setupSecurityQuestionDropdown(autoCompleteTextViewQuestion);
        setupSecurityQuestionDropdown(autoCompleteTextViewQuestion2);
        setupSecurityQuestionDropdown(autoCompleteTextViewQuestion3);

        addTextChangeListener(autoCompleteTextViewQuestion);
        addTextChangeListener(autoCompleteTextViewQuestion2);
        addTextChangeListener(autoCompleteTextViewQuestion3);

        buttonSubmit.setOnClickListener(view -> {
            saveSecurityQuestion();
        });
    }

    private void setupSecurityQuestionDropdown(AutoCompleteTextView autoCompleteTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(availableQuestions));
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
    }

    private void addTextChangeListener(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAvailableQuestions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateAvailableQuestions() {
        List<String> selectedQuestions = new ArrayList<>();
        if (!autoCompleteTextViewQuestion.getText().toString().isEmpty()) {
            selectedQuestions.add(autoCompleteTextViewQuestion.getText().toString());
        }
        if (!autoCompleteTextViewQuestion2.getText().toString().isEmpty()) {
            selectedQuestions.add(autoCompleteTextViewQuestion2.getText().toString());
        }
        if (!autoCompleteTextViewQuestion3.getText().toString().isEmpty()) {
            selectedQuestions.add(autoCompleteTextViewQuestion3.getText().toString());
        }

        availableQuestions = new ArrayList<>(Arrays.asList(securityQuestions));
        availableQuestions.removeAll(selectedQuestions);

        setDropdownAdapter(autoCompleteTextViewQuestion);
        setDropdownAdapter(autoCompleteTextViewQuestion2);
        setDropdownAdapter(autoCompleteTextViewQuestion3);
    }

    private void setDropdownAdapter(AutoCompleteTextView autoCompleteTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(availableQuestions));
        autoCompleteTextView.setAdapter(adapter);
    }

    private void saveSecurityQuestion() {
        String question1 = autoCompleteTextViewQuestion.getText().toString().trim();
        String question2 = autoCompleteTextViewQuestion2.getText().toString().trim();
        String question3 = autoCompleteTextViewQuestion3.getText().toString().trim();
        String answer1 = editTextSecurityAnswer.getText().toString().trim();
        String answer2 = editTextSecurityAnswer2.getText().toString().trim();
        String answer3 = editTextSecurityAnswer3.getText().toString().trim();

        if (question1.isEmpty() || question2.isEmpty() || question3.isEmpty() || answer1.isEmpty() || answer2.isEmpty() || answer3.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            getSharedPreferences("MyApp", MODE_PRIVATE)
                    .edit()
                    .putString("SECURITY_QUESTION_1", question1)
                    .putString("SECURITY_ANSWER_1", answer1)
                    .putString("SECURITY_QUESTION_2", question2)
                    .putString("SECURITY_ANSWER_2", answer2)
                    .putString("SECURITY_QUESTION_3", question3)
                    .putString("SECURITY_ANSWER_3", answer3)
                    .apply();
            Toast.makeText(this, "Security question set successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetSecurityQuestionActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}