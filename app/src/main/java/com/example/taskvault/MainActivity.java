package com.example.taskvault;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.taskvault.Adapter.ToDoAdapter;
import com.example.taskvault.Model.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements onDialogCloseListner {

    private RecyclerView recyclerView;
    private FloatingActionButton mFab;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;

    private Button logout_btn;

//    private static final String WEATHER_API_KEY = "fb63096d03780511d88f4194f0877971";
//    private static final String WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather";

    private TextView weatherTextView;
    private TextView Weather_Info;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherTextView = findViewById(R.id.wetherText);
        Weather_Info = findViewById(R.id.Weather_Info);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String randomUserId = sharedPreferences.getString("randomUserId", "");

        Log.d("activity_main", "randomUserId details Main: " + randomUserId);

        recyclerView = findViewById(R.id.recycerlview);
        mFab = findViewById(R.id.floatingActionButton);
        logout_btn = findViewById(R.id.logout_btn);
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked OK, perform logout
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.apply();

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked Cancel, do nothing
                            }
                        });

                // Create and show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager() , AddNewTask.TAG);
            }
        });

        mList = new ArrayList<>();
        adapter = new ToDoAdapter(MainActivity.this , mList);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Call a method to get weather information
        getWeatherInfo("London"); // Replace with the desired city

        showData();
        recyclerView.setAdapter(adapter);
    }

    private void getWeatherInfo(String city) {
        new WeatherTask().execute(city);
    }

    // AsyncTask to fetch weather information in the background
    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Nullable
        @Override
        protected String doInBackground(@NonNull String... params) {
            String city = params[0];
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=Colombo,srilanka&APPID=fb63096d03780511d88f4194f0877971");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder json = new StringBuilder(1024);
                String tmp;

                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp).append("\n");
                }

                reader.close();
                return json.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String json) {
            if (json != null) {
                try {
                    JSONObject data = new JSONObject(json);
                    double temperatureKelvin = data.getJSONObject("main").getDouble("temp");
                    double temperatureCelsius = temperatureKelvin - 273.15;
                    String description = data.getJSONArray("weather").getJSONObject(0).getString("description");
                    String cityName = data.getString("name");
                    String country = data.getJSONObject("sys").getString("country");

                    // Format the temperature with two decimal places
                    @SuppressLint("DefaultLocale") String formattedTemperature = String.format("%.2f", temperatureCelsius);

                    // Create a detailed weather information string
                    String weatherDetails = "Temperature: " + formattedTemperature + "°C\n"
                            + "Description: " + description;

                    // Log the weather information
                    Log.d("WeatherInfo", weatherDetails);

                    // Update the TextView with the detailed weather information
                    weatherTextView.setText(weatherDetails);
                    Weather_Info.setText(cityName +","+ country+" Weather Info");

                    // You might want to update your UI with the weather information here

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void showData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String randomUserId = sharedPreferences.getString("randomUserId", "");

        Log.d("randomUserId", "randomUserId details Main: " + randomUserId);
        if (!randomUserId.isEmpty()) {
            query = firestore.collection("task")
                    .whereEqualTo("id", randomUserId)  // Filter tasks by user ID
                    .orderBy("time", Query.Direction.DESCENDING);

            Log.d("query", "randomUserId details Main: " + query);

            listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String id = documentChange.getDocument().getId();
                                ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);
                                mList.add(toDoModel);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    // Note: Check for errors and handle them accordingly

                    // Remove the snapshot listener when done
                    if (listenerRegistration != null) {
                        listenerRegistration.remove();
                    }
                }
            });
        } else {
            // Handle the case where randomUserId is null or empty
            // You may want to show an error message or handle it in a way that makes sense for your app
        }
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }
}
