package com.example.notificationstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationstore.Adapter.NotificationAdapter;
import com.example.notificationstore.Model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationModels;
    private FirebaseAuth mAuth;
    ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        imageView2 = findViewById(R.id.imageView2);

        RecyclerView recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationModels = new ArrayList<>();
//        notificationModels.add(new NotificationModel("Facebook", "harry sent you a request",  2024-12-1 9:40 , R.drawable.facebook));
//        notificationModels.add(new NotificationModel("Instagram", "harry sent you a request", "Date/Time: 2024-12-1 9:40 PM", R.drawable.facebook));
//        notificationModels.add(new NotificationModel("Twitter", "harry sent you a request", "Date/Time: 2024-12-1 9:40 PM", R.drawable.facebook));
//        notificationModels.add(new NotificationModel("Whatsapp", "harry sent you a request", "Date/Time: 2024-12-1 9:40 PM", R.drawable.facebook));
//        notificationModels.add(new NotificationModel("Facebook", "harry sent you a request", "Date/Time: 2024-12-1 9:40 PM", R.drawable.facebook));

        mAuth = FirebaseAuth.getInstance();
        notificationAdapter = new NotificationAdapter(this, notificationModels);
        recyclerView.setAdapter(notificationAdapter);

        imageView2.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel notificationModel = snapshot.getValue(NotificationModel.class);
                    notificationModels.add(notificationModel);
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}