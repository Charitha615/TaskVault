package com.example.taskvault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText;

    public static final String ERROR_EMPTY_USERNAME = "Username cannot be empty";
    public static final String ERROR_EMPTY_PASSWORD = "Password cannot be empty";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usernameError = validateUsername();
                String passwordError = validatePassword();

                if (usernameError != null) {
                    loginUsername.setError(usernameError);
                }

                if (passwordError != null) {
                    loginPassword.setError(passwordError);
                }

                if (usernameError == null && passwordError == null) {
                    // Validation successful, proceed with login
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public String validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            return ERROR_EMPTY_USERNAME;
        } else {
            return null; // No error
        }
    }

    public String validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            return ERROR_EMPTY_PASSWORD;
        } else {
            return null; // No error
        }
    }

    public void checkUser() {
        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");

        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        reference.child("user_registration").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean loginSuccessful = false;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    HelperClass user = userSnapshot.getValue(HelperClass.class);

                    if (user != null && user.getUsername().equals(userUsername) && user.getPassword().equals(userPassword)) {
                        // Login successful
                        loginSuccessful = true;

                        String name = user.getName();
                        String email = user.getEmail();
                        String username = user.getUsername();
                        String randomUserId = userSnapshot.getKey();

                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("randomUserId", randomUserId);
                        editor.apply();

                        // Pass this data to the MainActivity using Intent
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish(); // Finish the LoginActivity to prevent going back to it with the back button
                        break; // Exit the loop since we found a match
                    }
                }

                if (!loginSuccessful) {
                    // Display an error message for invalid credentials
                    loginPassword.setError(ERROR_INVALID_CREDENTIALS);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }
}
