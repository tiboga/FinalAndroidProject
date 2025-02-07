package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yandex.mapkit.MapKitFactory;

public class MainGlobal extends AppCompatActivity {
    Button volunteer_task, zak_task;
    public static boolean isMapKitInitialized = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isMapKitInitialized) {
            MapKitFactory.setApiKey("0125ed02-7d2f-4c15-b356-4165d801ff31");
            MapKitFactory.initialize(this);
            isMapKitInitialized = true;
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_global);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        volunteer_task = findViewById(R.id.volunteer_task);
        volunteer_task.setOnClickListener(v -> {
            startActivity(new Intent(MainGlobal.this, VolunteerTasks.class));
        });
        zak_task = findViewById(R.id.zak_tasks);
        zak_task.setOnClickListener(v -> {
            startActivity(new Intent(MainGlobal.this, ZakTasks.class));
        });;
    }
}