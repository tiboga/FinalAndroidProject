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
    Button login, registration, hello, placemark, toglobalmain;

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
        toglobalmain = findViewById(R.id.toglobalmain);
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
        toglobalmain.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainGlobal.class);
            startActivity(intent);
        });

    }
}