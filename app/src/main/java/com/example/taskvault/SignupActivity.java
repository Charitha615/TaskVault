package com.example.taskvault;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input fields
                if (validateInputs()) {
                    // Proceed with user registration
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference();

                    String name = signupName.getText().toString();
                    String email = signupEmail.getText().toString();
                    String username = signupUsername.getText().toString();
                    String password = signupPassword.getText().toString();

                    Log.d("LoginActivity", "Checking username: " + username);
                    Log.d("LoginActivity", "Checking password: " + password);

                    String randomUserId = UUID.randomUUID().toString();

                    HelperClass helperClass = new HelperClass(name, email, username, password);
                    reference.child("users").child("user_registration").child(randomUserId).setValue(helperClass);

                    Toast.makeText(SignupActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(signupName.getText().toString())) {
            signupName.setError("Please enter your name");
            return false;
        } else if (!isAlpha(signupName.getText().toString())) {
            signupName.setError("Name should contain only alphabetic characters");
            return false;
        }

        if (TextUtils.isEmpty(signupEmail.getText().toString())) {
            signupEmail.setError("Please enter your email");
            return false;
        } else if (!isValidEmail(signupEmail.getText().toString())) {
            signupEmail.setError("Please enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(signupUsername.getText().toString())) {
            signupUsername.setError("Please enter your username");
            return false;
        }

        if (TextUtils.isEmpty(signupPassword.getText().toString())) {
            signupPassword.setError("Please enter your password");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        // Add your email validation logic here, for example using a regular expression
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isAlpha(String name) {
        // Check if the string contains only alphabetic characters
        return name.matches("[a-zA-Z]+");
    }
}
