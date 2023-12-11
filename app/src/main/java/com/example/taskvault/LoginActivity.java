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
                if (!validateUsername() | !validatePassword()){

                } else {
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

    public Boolean validateUsername(){
        String val = loginUsername.getText().toString();
        if (val.isEmpty()){
            loginUsername.setError("Username cannot be empty");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = loginPassword.getText().toString();
        if (val.isEmpty()){
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser(){

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
// Replace "your-database-reference" with the actual reference you want to use
        DatabaseReference reference = database.getReference("users");

        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        Log.d("LoginActivity", "Checking userUsername: " + userUsername);
        Log.d("LoginActivity", "Checking userPassword: " + userPassword);
        reference.child("user_registration").addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                    assert user != null;
//                    Log.d("LoginActivity", "user details : " + user);
                    if (user.getUsername().equals(userUsername) && user.getPassword().equals(userPassword)) {
                        // Login successful

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
                    else
                    {
                        Log.d("LoginActivity", "not login");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void printDatabaseStructure(DataSnapshot snapshot, int depth) {
        Log.d("SS","Hello");
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            StringBuilder indentation = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                indentation.append("  "); // Use two spaces for each level of indentation
            }

            String key = childSnapshot.getKey();
            Object value = childSnapshot.getValue();

            Log.d("DatabaseStructure", indentation + key + ": " + value);

            if (childSnapshot.hasChildren()) {
                printDatabaseStructure(childSnapshot, depth + 1);
            }
        }
        Log.d("SS","asdasdasdsad");
    }
}