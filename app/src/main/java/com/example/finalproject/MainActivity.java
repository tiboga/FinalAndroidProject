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

public class MainActivity extends AppCompatActivity {
    Button login, registration, hello, placemark, to_global_main, to_search_tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        login = findViewById(R.id.text);
        registration = findViewById(R.id.registration);
        hello = findViewById(R.id.hello);
        placemark = findViewById(R.id.place_on_map);
        to_global_main = findViewById(R.id.toglobalmain);
        to_search_tasks = findViewById(R.id.tosearchtasks);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });
        placemark.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectPlaceOnMap.class);
            startActivity(intent);
        });
        registration.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Registration.class);
            startActivity(intent);
        });
        hello.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Hello.class);
            startActivity(intent);
        });
        to_global_main.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainGlobal.class);
            startActivity(intent);
        });
        to_search_tasks.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchTaskToVolunteer.class);
            startActivity(intent);
        });

    }
}