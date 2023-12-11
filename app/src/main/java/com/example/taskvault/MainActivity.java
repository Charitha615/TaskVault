package com.example.taskvault;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

public class MainActivity extends AppCompatActivity implements onDialogCloseListner{

    private RecyclerView recyclerView;
    private FloatingActionButton mFab;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String randomUserId = sharedPreferences.getString("randomUserId", "");

        Log.d("activity_main", "randomUserId details Main: " + randomUserId);

        recyclerView = findViewById(R.id.recycerlview);
        mFab = findViewById(R.id.floatingActionButton);
        firestore = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

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
        showData();
        recyclerView.setAdapter(adapter);
    }
    private void showData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String randomUserId = sharedPreferences.getString("randomUserId", "");

        if (randomUserId != null && !randomUserId.isEmpty()) {
            query = firestore.collection("task")
                    .whereEqualTo("id", randomUserId)  // Filter tasks by user ID
                    .orderBy("time", Query.Direction.DESCENDING);

            listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
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